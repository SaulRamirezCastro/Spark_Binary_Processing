package com.dynatron

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.github.plokhotnyuk.jsoniter_scala.MainRecordParser
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.spark.sql.functions.{input_file_name, split}
import org.apache.spark.sql.{Dataset, SparkSession}


object App2 {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder.master("local").appName("Simple Application")
      .config("spark.hadoop.fs.s3a.aws.credentials.provider", "com.amazonaws.auth.profile.ProfileCredentialsProvider")
      .getOrCreate()

    import spark.implicits._

    val binaries = spark.read.format("binaryFile").load("s3a://sramirez-geep-demo/row_data/*.tar.gz")

    val records: Dataset[MainRecord] = binaries.select($"path").as[String].flatMap(
      objectName => {
        val s3 = AmazonS3ClientBuilder.standard.withRegion(Regions.US_WEST_1).build
        val (bucketName, key) = objectName.splitAt(objectName.indexOfSlice("/", "s3a://".length))
        val s3object = s3.getObject(bucketName.substring(6), key.substring(1))
        val gzi = new GzipCompressorInputStream(s3object.getObjectContent)
        val archive = new TarArchiveInputStream(gzi)
        Stream.continually(archive.getNextEntry).
          takeWhile(_ ne null).
          filter(e => !e.isDirectory && !e.getName.startsWith("._")). // filter out mac files for testing
          flatMap(_ => new MainRecordParser().parse(archive))
      }
    )

    val recordsWithFolder = records.withColumn("folder_id", split(input_file_name,"/").getItem(6))

    recordsWithFolder.write.partitionBy("folder_id").mode("overwrite").parquet("s3a://sramirez-geep-demo/output_files")
  }
}

package com.github.plokhotnyuk.jsoniter_scala

import com.dynatron.{Ros, MainRecord}
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, ReaderConfig}

import java.io.InputStream

class MainRecordParser(rosParser: RosParser = new RosParser) {

  def parse(inputStream: InputStream): Iterator[MainRecord] = {
    val mainRecordBuilder = new MainRecordBuilder()

    val jsonReader = new JsonReader(
      in = inputStream,
      config = ReaderConfig.withThrowReaderExceptionWithStackTrace(true)
    ) // 1 MB buffer size
    jsonReader.nextToken()
    while (jsonReader.skipWhitespaces() && !jsonReader.isCurrentToken('}')) {
      val fieldName = jsonReader.readKeyAsString
      fieldName match {
        case "von" => mainRecordBuilder.von = new String(jsonReader.readRawValAsBytes)
        case "client" => mainRecordBuilder.client = new String(jsonReader.readRawValAsBytes)
        case "package" => mainRecordBuilder.packageRos = new String(jsonReader.readRawValAsBytes)
        case "ros" => mainRecordBuilder.rosIterator = rosParser.parse(jsonReader)
          return mainRecordBuilder
        case null =>
        case _ => throw new IllegalStateException(s"unsupported field name: $fieldName")
      }
      jsonReader.nextToken()
    }
    throw new IllegalStateException(s"no ros field found")
  }
}


class RosParser {
  def parse(jsonReader: JsonReader): Iterator[Ros] = {
    new Iterator[Ros] {
      override def hasNext: Boolean = jsonReader.nextToken().asInstanceOf[Char] != '}'
      override def next(): Ros = {
        val rosKey = jsonReader.readKeyAsString
        val rosValue = new String(jsonReader.readRawValAsBytes)
        Ros(rosKey, rosValue)
      }
    }
  }
}

class MainRecordBuilder() extends Iterator[MainRecord] {
  var von: String = null
  var client: String = null
  var packageRos: String = null
  var rosIterator: Iterator[Ros] = Iterator.empty

  override def hasNext: Boolean = rosIterator.hasNext

  override def next(): MainRecord = MainRecord(von, client, packageRos, rosIterator.next)
}

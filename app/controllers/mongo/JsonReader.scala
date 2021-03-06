package controllers.mongo

import controllers.parsers.WebPageElement
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.json.Reads._
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONObjectID


case class ConfigurationSyntax(sentence: String, typed_sentence: String)
case class ConfigurationRow(group: String, name: String, syntax: List[ConfigurationSyntax])
case class MacroConfiguration(id: Option[String], cType: String, rows: List[ConfigurationRow])
case class AutoSetupConfig(id: Option[String], name: String, cType: String, rows: Option[List[WebPageElement]])
case class InspectedPage(name: String, items: List[String])
case class InspectedScenario(name: String, steps: String)
case class Scenario(id: Option[String], name: String, cType: String, driver: String, rows: Option[String])
case class TestScript(id: Option[String], name: String, scenarii: List[Scenario])


object InspectedScenario{
  implicit val reader: Reads[InspectedScenario]= (
      (__ \ "name").read[String] and
      (__ \ "steps").read[String])(InspectedScenario.apply(_,_))

  implicit val writer: Writes[InspectedScenario] = (
      (__ \ "name").write[String] and
      (__ \ "steps").write[String])(unlift(InspectedScenario.unapply))
}

object InspectedPage{
	implicit val reader: Reads[InspectedPage]= (
      (__ \ "name").read[String] and
      (__ \ "items").read[List[String]])(InspectedPage.apply(_,_))

	implicit val writer: Writes[InspectedPage] = (
      (__ \ "type").write[String] and
      (__ \ "items").write[List[String]])(unlift(InspectedPage.unapply))
}

object Scenario{
  implicit val reader: Reads[Scenario]= (
      (__ \ "id").readNullable[String] and
	  (__ \ "name").read[String] and
      (__ \ "type").read[String] and
      (__ \ "driver").read[String] and
      (__ \ "rows").readNullable[String])(Scenario.apply(_,_,_,_,_))

  implicit val writer: Writes[Scenario] = (
      (__ \ "id").writeNullable[String] and
      (__ \ "name").write[String] and
      (__ \ "type").write[String] and
      (__ \ "driver").write[String] and
      (__ \ "rows").writeNullable[String])(unlift(Scenario.unapply))

  implicit object BSONWriter extends BSONDocumentWriter[Scenario] {
    def write(scenario: Scenario): BSONDocument =
      scenario.id match {
        case None =>  BSONDocument("name"-> scenario.name, "type"-> scenario.cType, "driver" -> scenario.driver,  "rows" -> scenario.rows.getOrElse(""))
        case value:Option[String] => BSONDocument("_id" -> BSONObjectID(value.get), "name" -> scenario.name, "type"-> scenario.cType,
                                                  "driver" -> scenario.driver,  "rows" -> scenario.rows.getOrElse(""))
      }
  }

  implicit object BSONReader extends BSONDocumentReader[Scenario] {
    def read(doc: BSONDocument): Scenario = {
      val id = doc.getAs[BSONObjectID]("_id").get.stringify
      val name = doc.getAs[String]("name").get
      val scenarioType = doc.getAs[String]("type").get
      val driver = doc.getAs[String]("driver").get
      val rows = doc.getAs[String]("rows").getOrElse("")
      Scenario(Option[String](id), name ,scenarioType, driver, Option[String](rows))
    }
  }
}


object TestScript{
  implicit val format = Json.format[TestScript]
  implicit object BSONWriter extends BSONDocumentWriter[TestScript] {
    def write(testScript: TestScript): BSONDocument =
      testScript.id match {
        case None =>  BSONDocument("name"-> testScript.name, "scenarii" -> testScript.scenarii)
        case value:Option[String] => BSONDocument("_id" -> BSONObjectID(value.get), "name"-> testScript.name,
          "scenarii" -> testScript.scenarii)
      }
  }

  implicit object BSONReader extends BSONDocumentReader[TestScript] {
    def read(doc: BSONDocument): TestScript = {
      val id = doc.getAs[BSONObjectID]("_id").get.stringify
      val name = doc.getAs[String]("name").get
      val scenarii = doc.getAs[List[Scenario]]("scenarii").get
      TestScript(Option[String](id), name ,scenarii)
    }
  }
}


object AutoSetupConfig{
    implicit val autoSetupConfigReader: Reads[AutoSetupConfig]= (
      (__ \ "id").readNullable[String] and
      (__ \ "name").read[String] and
      (__ \ "type").read[String] and
      (__ \ "rows").readNullable[List[WebPageElement]])(AutoSetupConfig.apply(_,_ , _,_)
    )

    implicit val autoSetupConfigWriter: Writes[AutoSetupConfig] = (
    (__ \ "id").writeNullable[String] and
    (__ \ "name").write[String] and
    (__ \ "type").write[String] and
    (__ \ "rows").writeNullable[List[WebPageElement]])(unlift(AutoSetupConfig.unapply))

  implicit object AutoSetupConfigurationWriter extends BSONDocumentWriter[AutoSetupConfig] {
    def write(configuration: AutoSetupConfig): BSONDocument = 
      configuration.id match {
      	case None =>  BSONDocument("name"-> configuration.name, "type" -> configuration.cType,  "rows" -> configuration.rows.getOrElse(List()))
      	case  value:Option[String] => BSONDocument("_id" -> BSONObjectID(value.get), "name"-> configuration.name,  "type" -> configuration.cType,  "rows" -> configuration.rows.getOrElse(List()))
      }
  }

  implicit object AutoSetupConfigurationReader extends BSONDocumentReader[AutoSetupConfig] {
    def read(doc: BSONDocument): AutoSetupConfig = {
      val id = doc.getAs[BSONObjectID]("_id").get.stringify
      val name = doc.getAs[String]("name").get
      val ctype = doc.getAs[String]("type").get
      val rows = doc.getAs[List[WebPageElement]]("rows").getOrElse(List())
      AutoSetupConfig(Option[String](id), name, ctype, Option[List[WebPageElement]](rows))
    }
  }
}

object ConfigurationSyntax {

  implicit val configSyntaxReader: Reads[ConfigurationSyntax] = (
    (__ \ "sentence").read[String]
    and (__ \ "typed_sentence").read[String])(ConfigurationSyntax.apply(_,_))

  implicit val configSyntaxWriter: Writes[ConfigurationSyntax] = (
    (__ \ "sentence").write[String] and
    (__ \ "typed_sentence").write[String])(unlift(ConfigurationSyntax.unapply))

  implicit object ConfigurationSyntaxReader extends BSONDocumentReader[ConfigurationSyntax] {
    def read(doc: BSONDocument): ConfigurationSyntax = {
      val sentence = doc.getAs[String]("sentence").get
      val typed_sentence = doc.getAs[String]("typed_sentence").get
      ConfigurationSyntax(sentence, typed_sentence)
    }
  }

  implicit object ConfigurationSyntaxWriter extends BSONDocumentWriter[ConfigurationSyntax] {
    def write(configurationSyntax: ConfigurationSyntax): BSONDocument = BSONDocument(
      "sentence" -> configurationSyntax.sentence,
      "typed_sentence" -> configurationSyntax.typed_sentence)
  }

}

object ConfigurationRow {

  implicit val configRowReader: Reads[ConfigurationRow] = (
    (__ \ "type").read[String] and
    (__ \ "name").read[String] and 
    (__ \ "syntax").read[List[ConfigurationSyntax]])(ConfigurationRow.apply(_,_,_))

  implicit val configRowWriter: Writes[ConfigurationRow] = (
    (__ \ "type").write[String] and
    (__ \ "name").write[String] and
    (__ \ "syntax").write[List[ConfigurationSyntax]])(unlift(ConfigurationRow.unapply))

  implicit object ConfigurationRowWriter extends BSONDocumentWriter[ConfigurationRow] {
    def write(configurationRow: ConfigurationRow): BSONDocument = BSONDocument(
      "type" -> configurationRow.group,
      "name" -> configurationRow.name,
      "syntax" -> configurationRow.syntax)
  }

  implicit object ConfigurationRowReader extends BSONDocumentReader[ConfigurationRow] {
    def read(doc: BSONDocument): ConfigurationRow = {
      val group = doc.getAs[String]("type").get
      val name = doc.getAs[String]("name").get
      val syntax = doc.getAs[List[ConfigurationSyntax]]("syntax").get
      ConfigurationRow(group, name, syntax)
    }
  }

}

object MacroConfiguration {

  implicit val configReader: Reads[MacroConfiguration] = (
    (__ \ "id").readNullable[String] and  
    (__ \ "type").read[String]
    and (__ \ "rows").read[List[ConfigurationRow]])(MacroConfiguration.apply(_, _, _))

  implicit val configWriter: Writes[MacroConfiguration] = (
    (__ \ "id").writeNullable[String] and  
    (__ \ "type").write[String] and
    (__ \ "rows").write[List[ConfigurationRow]])(unlift(MacroConfiguration.unapply))

  implicit object ConfigurationWriter extends BSONDocumentWriter[MacroConfiguration] {
    def write(configuration: MacroConfiguration): BSONDocument = 
      configuration.id match {
      	case None =>  BSONDocument("type" -> configuration.cType, "rows" -> configuration.rows)
      	case  value:Option[String] => BSONDocument("_id" -> BSONObjectID(value.get), "type" -> configuration.cType, "rows" -> configuration.rows)
      }
  }

  implicit object ConfigurationReader extends BSONDocumentReader[MacroConfiguration] {
    def read(doc: BSONDocument): MacroConfiguration = {
      val id = doc.getAs[BSONObjectID]("_id").get.stringify
      val ctype = doc.getAs[String]("type").get
      val rows = doc.getAs[List[ConfigurationRow]]("rows").get
      MacroConfiguration(Option[String](id), ctype, rows)
    }
  }
}


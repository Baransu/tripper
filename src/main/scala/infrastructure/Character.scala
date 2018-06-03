package infrastructure

trait Character {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Episode.Value]
}

case class Droid(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  primaryFunction: Option[String]
                ) extends Character

case class Human(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  homePlanet: Option[String]
                ) extends Character

package api

import infrastructure.{CharacterRepo, Droid, Episode, Human, Character}

class SangriaContext(characterRepo: CharacterRepo) {

  def getHumans: (Int, Int) => List[Human] = characterRepo.getHumans

  def getDroids: (Int, Int) => List[Droid] = characterRepo.getDroids

  def getHero: Option[Episode.Value] => Character = characterRepo.getHero

  def getHuman: String => Option[Human] = characterRepo.getHuman

  def getDroid: String => Option[Droid] = characterRepo.getDroid

 }
package dk.tennis.compare.rating.multiskill.matchloader

import java.util.Date
import dk.tennis.compare.rating.multiskill.model.perfdiff.Surface._

case class MatchResult(tournamentTime: Date, tournamentName: String,surface:Surface,player1: String, player2: String, matchTime:Date,player1Won: Boolean, numOfSets: Int, p1Stats: PlayerStats, p2Stats: PlayerStats) {

  require(numOfSets == 2 || numOfSets == 3, "The number of sets can be 2 or 3, but is " + numOfSets)

  def containsPlayer(playerName: String): Boolean = player1.equals(playerName) || player2.equals(playerName)
}
    
   
package dk.tennis.compare.rating.multiskill.model.perfdiff

import scala.math.log
import org.junit.Test
import com.typesafe.scalalogging.slf4j.LazyLogging
import dk.bayes.math.gaussian.Gaussian
import dk.tennis.compare.rating.multiskill.matchloader.MatchesLoader
import scala.io.Source
import scala.collection.immutable.HashSet
import scala.math._
import dk.tennis.compare.rating.multiskill.model.perfdiff.factorgraph.SkillsFactorGraph
import breeze.linalg.DenseVector
import dk.tennis.compare.rating.multiskill.scoresim.scoreSim
import dk.tennis.compare.rating.multiskill.infer.matchprob.MatchPrediction
import dk.tennis.compare.rating.multiskill.model.perfdiff.skillsfactor.cov.skillovertime.SkillOverTimeCovFunc
import dk.tennis.compare.rating.multiskill.infer.outcome.InferOutcomeGivenPerfDiff

class GenericPerfDiffTest extends LazyLogging {

   val (priorSkillOnServe, priorSkillOnReturn) = (4.61d, 0)
  val initialParams = DenseVector(log(1), log(30), log(1), log(365), 2.3)
  
//  val (priorSkillOnServe, priorSkillOnReturn) = (4.59d, 0)
//  val initialParams = DenseVector(-0.5014044059042678, 2.6478899229820803, 0.10034927453586179, 6.40731965340222, 2.3)
  val covarianceParams = initialParams.data.dropRight(1)
  val logPerfStdDev = initialParams.data.last

  val matchesFile = "./src/test/resources/atp_historical_data/match_data_2006_2013.csv"
  val matchResults = MatchesLoader.loadMatches(matchesFile, 2011, 2011)

  val realScores: Array[Score] = Score.toScores(matchResults)

  val scores = realScores //simulateScores(realScores)
  val playerNames: Array[String] = Score.toPlayers(scores).map(p => p.playerName).distinct

  logger.info(s"Players by name: ${playerNames.size}")
  logger.info(s"All games (on serve + on return): ${scores.size}")

  @Test def test {

    val infer = GenericPerfDiffModel(playerSkillMeanPrior, SkillOverTimeCovFunc(covarianceParams), logPerfStdDev, scores)
    infer.calibrateModel()
    // println(infer.skillsFactor.getPriorSkillsForPlayer("Roger Federer", true).v)

    logger.info("Calculating log likelihood")

    val perfDiffs = infer.inferPerfDiffs()

    val loglik = InferOutcomeGivenPerfDiff.totalLoglik(perfDiffs.map(p => p.perfDiff), scores, score => { score.player1.playerName.equals("Roger Federer"); true })

    println("Total/avg log lik: %.3f/%.4f".format(loglik, loglik / scores.map(s => s.pointsWon.get._1 + s.pointsWon.get._2).sum))

    // println("Player skills on serve")
    //  val p1SkillsMean = infer.calcPosteriorSkillsForPlayer("Roger Federer", true).m
    //  println(p1SkillsMean)

  }

  private def playerSkillMeanPrior(player: Player): Double = {
    if (player.onServe) priorSkillOnServe else priorSkillOnReturn

  }

  private def simulateScores(realScores: Array[Score]): Array[Score] = {
    val meanFunc = (player: Player) => { if (player.onServe) priorSkillOnServe else priorSkillOnReturn }
    val covFunc = SkillOverTimeCovFunc(initialParams.data.take(4))
    val simScores = scoreSim(realScores, meanFunc, covFunc, logPerfStdDev = initialParams.data.last,randSeed=555677675)
    val scores = simScores.map(s => s.score)
    scores
  }

}
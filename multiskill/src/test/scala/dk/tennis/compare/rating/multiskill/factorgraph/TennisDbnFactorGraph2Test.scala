package dk.tennis.compare.rating.multiskill.factorgraph

import scala.math.pow
import scala.util.Random
import org.junit.Test
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import dk.atp.api.CSVATPMatchesLoader
import dk.atp.api.domain.SurfaceEnum.HARD
import dk.bayes.infer.ep.GenericEP
import dk.bayes.infer.ep.calibrate.fb.ForwardBackwardEPCalibrate
import org.apache.commons.lang.time.StopWatch
import dk.tennis.compare.rating.multiskill.domain.PointResult
import dk.tennis.compare.rating.multiskill.domain.MatchResult
import dk.tennis.compare.rating.multiskill.testutil.MultiSkillTestUtil._
import dk.tennis.compare.rating.multiskill.domain.MultiSkillParams
import dk.tennis.compare.rating.multiskill.domain.PlayerSkill

class TennisDbnFactorGraph2Test {

  val logger = Logger(LoggerFactory.getLogger(getClass()))

  val matchResults = loadTennisMatches(2011, 2011)

  val tournaments = toTournaments(matchResults)

  val multiSkillParams = MultiSkillParams(
    skillOnServeTransVariance = 0.02,
    skillOnReturnTransVariance = 0.02,
    priorSkillOnServe = PlayerSkill(0, 1), priorSkillOnReturn = PlayerSkill(0, 1),
    perfVarianceOnServe = 200, perfVarianceOnReturn = 100)

  @Test def calibrate {

    println("Results num:" + matchResults.size)

    val tennisFactorGraph = TennisDbnFactorGraph2(multiSkillParams)

    tournaments.foreach(t => tennisFactorGraph.addTournament(t))

    println("Factors num: " + tennisFactorGraph.getFactorGraph.getFactorNodes.size)
    println("Variables num: " + tennisFactorGraph.getFactorGraph.getVariables.size)

    val timer = new StopWatch()
    timer.start()

    val epCalibrate = ForwardBackwardEPCalibrate(tennisFactorGraph.getFactorGraph())
    val iterTotal = epCalibrate.calibrate(100, progress)
    logger.debug("Iter total: " + iterTotal)
    logger.debug("Time: " + timer.getTime())

  }

  private def progress(currIter: Int) = println("EP iteration: " + currIter)
}
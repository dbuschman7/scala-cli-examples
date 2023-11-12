package me.lightspeed7.examples.thrift

import me.lightspeed7.examples.thrift.TradeHistory.service.ServiceException

package object TradeHistory {

  sealed trait TradeType {
    def ordinal: Int
  }

  case object GOOD extends TradeType {
    val ordinal: Int = 1
  }

  case object BAD extends TradeType {
    val ordinal: Int = 2
  }

  case object UGLY extends TradeType {
    val ordinal: Int = 3
  }

  type S3URL = String
  type someMap = Map[some_dir.Type, S3URL]
  type urlList = Seq[S3URL]

  val LongLargeValue: Long = 2345234523453452345L

  final case class TradeReport(
                                symbol: String,
                                price: Double,
                                size: Int,
                                seq_num: Int,
                                classType: some_dir.Type,
                                optionalInt: Option[Int]
                              )


  final case class GetLastSaleParams(
                                        Symbol: String,
                                        status: TradeType,

                                      )

  final case class GetLastSaleRequest(
                                         params: GetLastSaleParams,
                                         context: service.RpcContext
                                       )

  final case class GetLastSaleResponse(
                                        returnValue: Option[TradeReport],
                                        exception: Option[ServiceException]
                                      )

}

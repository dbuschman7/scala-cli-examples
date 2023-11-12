package me.lightspeed7.examples.thrift.TradeHistory

// structs
trait TradeHistory_get_last_sale_Generated extends another_dir.BaseService {

 //  def get_last_sale(request: GetLastSaleRequest): TradeReportResponse
}


//
// helpers
object some_dir {
  type Type = String
}

object service {
  final case class ServiceException(code: Int, message: String)

  final case class RpcContext(serviceName: String,
                              methodName: String,
                              traceId: String,
                              customerId: Int)
}

object another_dir {
  trait BaseService {

  }
}
namespace * TradeReporting

include "types/some_dir/foo.thrift"
#single line comment

enum TradeType {
    GOOD = 1
    BAD = 2
    UGLY = 4
}

typedef string S3URL
typedef map<some_dir.Type, S3URL> someMap
typedef list<S3URL>  urlList

const i64 LongLargeValue = 2345234523453452345

struct TradeReport {
    1: string  symbol,
    2: double  price,
    3: i32     size, // end of line comment
    4: i32     seq_num,
    5: some_dir.Type classType,
    6: optional i32 optionalInt
    // another single line comment

}

service TradeHistory extends another_dir.BaseService {
    /**
    * Multi-line comment
    **/
    TradeReport get_last_sale(1: string Symbol, 2:  TradeType status) throws (1: service.ServiceException ex)
}
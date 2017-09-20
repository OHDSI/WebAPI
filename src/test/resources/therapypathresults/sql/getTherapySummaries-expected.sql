select lookupkey, lookupname, sum(tx1) tx1, sum(tx2) tx2, sum(tx3) tx3, sum(tx_total) total 
from txsummary 
where ReportId in ( 
  ?
) 
group by lookupkey, lookupname

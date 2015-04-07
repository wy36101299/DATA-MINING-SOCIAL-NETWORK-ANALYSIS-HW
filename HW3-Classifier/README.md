# DATA MINING & SOCIAL NETWORK 
### HW3 - Classifier 4/8
### 電通所-Q36034188-吳典陽   
### data: 
test.txt 5KB  
training.txt 12KB
### File: HW3-Classifier.py  
method : decision tree(ID3)
### result: 
準確率 56%  97/171  
下表為test的answer和分類器的answer做比較  
![Mou icon](http://hpdswy.ee.ncku.edu.tw/~wy/image/datamininghw3.png)    

### link
[ipynb online](http://nbviewer.ipython.org/url/hpdswy.ee.ncku.edu.tw/~wy/ipynb/HW3-Classifier.ipynb)  
[github](https://github.com/wy36101299/DATA-MINING-SOCIAL-NETWORK-ANALYSIS-HW)

### 感想
data processing 很麻煩，一堆缺值，要先濾掉，再來因為要丟到decision tree(ID3)，只接受離散的data，不接受連續，需要再把連續的data轉成離散的，轉換規則為 四個等級  
`離散化 把numeric分成四個等級 4 > (max+mean/2) > 3 > mean > 2 >(mean+min/2) > 1`  
後來發現這個feature:marital_status 結果都是M，餵進分類器中會讓分類器爆掉，是一個很沒意義的feature，就決定拿掉所以後來剩下三個feature: `num_children_at_home` ,`age`,`year_income`。  
  
分類器的準確率很不理想只有56%，根本就是弱分類器，後來去計算一下結果做成table，發現Gold和Basic都猜得蠻準確的，但是Silver和Normal都猜不出來，都會猜Basic，可能這邊出了點問題，還要回去看怎樣切的，切不出Silver和Normal。

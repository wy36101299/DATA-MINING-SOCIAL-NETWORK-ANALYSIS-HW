# DATA MINING & SOCIAL NETWORK 
### hw4-Clustering 4/15
### 電通所-Q36034188-吳典陽   
kmeans
======
k-means 是一個聚類 (Cluster) 的方式，依照著物以類聚去分群  
流程如下：  
1. 隨機選取data中的k筆資料當作初始群中心c1~ck  
2. 計算每個資料xi 對應到最短距離的群中心 (固定 ci 求解所屬群 Si)  
3. 利用目前得到的分類重新計算群中心 (固定 Si 求解群中心 ci)  
4. 重複step 2,3直到收斂 (達到最大疊代次數 or 群心中移動距離很小)  

###advance k-means
--------------
為解決k-means不穩定問題(初始中心沒選好)而利用重覆多次尋找其sse最小  
(sum of square error每個資料xi 對應到最短距離的群中心的平方總和)  
找到較好的初始點時在帶入k-means做分群。  

### how to choose better k
--------------
根據elbow theorem找到凸出的點為較好的k值，其原理是比較其斜率，ssei-sse/ki-k，k越多，sse則  一定越小，但是尋找sse下降幅度最多的k值就可能是更好的k值。  

### bisecting K means
--------------
二元k-means，其中心思想為從原本一群分成兩群，再從其中一群在分成兩群，直到指定的k群時  
由sse來判斷哪一群誤差有點大該被分群，其效率會比原本的k-means好，原因為只要運算被分到的群  
不用全部去運算。  

### data: 
0_random.txt 452byte  
![0_random](http://hpdswy.ee.ncku.edu.tw/~wy/image/0_random.png)  
1_random.txt 9KB  
![1_random](http://hpdswy.ee.ncku.edu.tw/~wy/image/1_random.png)

### File: k-means.py  
### result: 
0_randomRes.txt 用 k-means的結果  
![0_randomRes.txt](http://hpdswy.ee.ncku.edu.tw/~wy/image/0_randomkmeans.png)  
0_randomResBis.txt 用 bisecting K-means的結果  
![0_randomResBis.txt](http://hpdswy.ee.ncku.edu.tw/~wy/image/0_randomBis.png)  
1_randomRes.txt 用 k-means的結果  
![1_randomRes.txt](http://hpdswy.ee.ncku.edu.tw/~wy/image/1_randomkmeans.png)  
1_randomResBis.txt 用 bisecting K-means的結果  
![1_randomResBis.txt](http://hpdswy.ee.ncku.edu.tw/~wy/image/1_randomBis.png)  
### 測試 1_random.txt 在不同k情況下的分群(k-means)  
k=2  
![k2](http://hpdswy.ee.ncku.edu.tw/~wy/image/k2.png)  
k=3  
![k3](http://hpdswy.ee.ncku.edu.tw/~wy/image/k3.png)  
k=4  
![1_randomRes.txt](http://hpdswy.ee.ncku.edu.tw/~wy/image/1_randomkmeans.png)  
k=5  
![k5](http://hpdswy.ee.ncku.edu.tw/~wy/image/k5.png)  
k=6  
![k6](http://hpdswy.ee.ncku.edu.tw/~wy/image/k6.png)  
k=7  
![k7](http://hpdswy.ee.ncku.edu.tw/~wy/image/k7.png)  

### 感想
k-means 和 bisecting K means 有測過時間，可是bisecting K means的時間沒有比較快，原因是因為dataset太小，有時測過倘若dataset夠大，那bisecting K means是會減少很多運算時間。  
從圖可以清楚看到，對於1_random.txt的分群並沒有分得很好，這就是k-means的缺點 。
### ppt
--------------
[k-means](http://www.slideshare.net/ssuserf88631/k-means-42435149)  

### ipython notebook
--------------
[線上看ipynb](http://nbviewer.ipython.org/url/hpdswy.ee.ncku.edu.tw/~wy/ipynb/k-means.ipynb)
### Github
--------------
[github](https://github.com/wy36101299/DATA-MINING-SOCIAL-NETWORK-ANALYSIS-HW/tree/master/hw4-Clustering)

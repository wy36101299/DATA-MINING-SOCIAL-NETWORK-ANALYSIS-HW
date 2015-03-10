
# coding: utf-8

# In[115]:

import json
import datetime
import numpy as np


# In[116]:

# 1,3,4
# 2,3,5
# 1,2,3,5
# 2,5
def loadDataSet():
    return [[1, 3, 4], [2, 3, 5], [1, 2, 3, 5], [2, 5]]
dataSet = loadDataSet()


# In[117]:

dataSet


# In[118]:

# 找出所有dataset中的item集合
# frozenset 和 set 最大的差異在於前者不可變,後者可變,可當作dict的key
def createC1(dataSet):
    C1 = []
    for transaction in dataSet:
        for item in transaction:
            if not [item] in C1:
                C1.append([item])    
    C1.sort()
#     print(C1)
    return map(frozenset, C1)#use frozen set so we
                            #can use it as a key in a dict 


# In[119]:

c1 = createC1(dataSet)


# In[120]:

c1


# In[121]:

# scan dataset 輸出高於minSupport的item
# dict supportData 儲存 item support value
def scanD(D, Ck, minSupport):
    ssCnt = {}
    # 算出每一個item總出現次數
    for tid in D:
        for can in Ck:
            if can.issubset(tid):
                if not ssCnt.has_key(can): ssCnt[can]=1
                else: ssCnt[can] += 1
    numItems = float(len(D))
    retList = []
    supportData = {}
# retList 儲存 >輸出高於minSupport的items集合
    for key in ssCnt:
        support = ssCnt[key]/numItems
        if support >= minSupport:
            # retList.insert(0,key) 從首插入
            # retList.append(key) 從尾插入
            retList.append(key)
        supportData[key] = support
    return retList, supportData , ssCnt


# In[122]:

retList, supportData , ssCnt = scanD(dataSet, c1, 0.5)


# In[123]:

# item4低於0.5
retList


# In[124]:

supportData


# In[127]:

# 從小集合聚合成大集合 兩個兩個聚合
def aprioriGen(Lk, k): #creates Ck
    retList = []
    lenLk = len(Lk)
    # 指針移動尋找集合  i,j各別移動
    for i in range(lenLk):
        for j in range(i+1, lenLk): 
            L1 = list(Lk[i])[:k-2]; L2 = list(Lk[j])[:k-2]
            # 先排序太精妙了 可以避免重覆的 屌啊～
            L1.sort(); L2.sort()
            if L1==L2: #if first k-2 elements are equal
                retList.append(Lk[i] | Lk[j]) #set union
    return retList

# 輸入dataset 從小集合聚合到大集合，輸出高於minSupport的item
def apriori(dataSet, minSupport = 0.5):
    C1 = createC1(dataSet)
    D = map(set, dataSet)
    L1, supportData ,ssCnt= scanD(D, C1, minSupport)
    L = [L1]
    k = 2
    while (len(L[k-2]) > 0):
        Ck = aprioriGen(L[k-2], k)
        Lk, supK ,ssCnt = scanD(D, Ck, minSupport)#scan DB to get Lk
        supportData.update(supK)
        L.append(Lk)
        k += 1
    return L, supportData


# In[128]:

L,supportData = apriori(dataSet,0.5)


# In[129]:

L


# In[131]:

supportData


# In[133]:

# 算出Rule
def generateRules(L, supportData, minConf=0.7):  #supportData is a dict coming from scanD
    bigRuleList = []
    # L[0] 集合只有一個item frozenset([5]) L[1]集合有兩個item frozenset([2, 5])
    for i in range(1, len(L)):#only get the sets with two or more items
        for freqSet in L[i]:
            H1 = [frozenset([item]) for item in freqSet]
            if (i > 1):
                rulesFromConseq(freqSet, H1, supportData, bigRuleList, minConf)
            else:
                # 計算 confidence
                calcConf(freqSet, H1, supportData, bigRuleList, minConf)
    return bigRuleList         

def calcConf(freqSet, H, supportData, brl, minConf=0.7):
    prunedH = [] #create new list to return
    for conseq in H:
        # freqSet U conseq / freqSet - conseq     豆奶：freqSet - conseq 青菜：conseq 豆奶U青菜：freqSet
        # 買了豆奶和青菜 豆奶 -> 青菜 買了豆奶之後，再買青菜的機率
        conf = supportData[freqSet]/supportData[freqSet-conseq] #calc confidence
        if conf >= minConf: 
            print freqSet-conseq,'-->',conseq,'conf:',conf
            brl.append((freqSet-conseq, conseq, conf))
            prunedH.append(conseq)
    return prunedH

def rulesFromConseq(freqSet, H, supportData, brl, minConf=0.7):
    # 每一個集合中的物品數
    m = len(H[0])
    if (len(freqSet) > (m + 1)): #try further merging
        Hmp1 = aprioriGen(H, m+1)#create Hm+1 new candidates
        Hmp1 = calcConf(freqSet, Hmp1, supportData, brl, minConf)
        if (len(Hmp1) > 1):    #need at least two sets to merge
            rulesFromConseq(freqSet, Hmp1, supportData, brl, minConf)


# In[134]:

rule = generateRules(L,supportData,0.7)


# In[135]:

rule


# In[136]:

# data source : retail.dat 4.2Mb


# In[137]:

datapath = '/Users/wy/Desktop/retail.dat'
dataList=[]
with open (datapath,'r') as f:
    lines = f.readlines()
for index, line in enumerate(lines):
    line=line.strip()
    listLine = map(int,line.split(' '))
    dataList.append(listLine)


# In[138]:

start = datetime.datetime.now()

L,supportData = apriori(dataList,0.1)
rule = generateRules(L,supportData,0.2)

end = datetime.datetime.now()
runtime = end - start


# In[139]:

runtime


# In[152]:

def store(data,path):
    with open(path,'w') as f:
        f.write(data)


# In[153]:

freItem_list = []
for tmp in L:
    if len(tmp) !=0:
        for item in tmp:
            freItem_list.append(list(item))
freItem_json = json.dumps(freItem_list,indent=4)
store(freItem_json,'/Users/wy/Desktop/freItem.json')


# In[158]:

freItem_list


# In[155]:

supportData_list = []
for key, value in supportData.iteritems():
    s = str(list(key))+':'+str(value)
    supportData_list.append(s)
supportData_list.sort()
supportData_json = json.dumps(supportData_list,indent=4)
store(supportData_json,'/Users/wy/Desktop/supportData.json')


# In[160]:

# 前10筆
supportData_list[:10]


# In[156]:

strongRule = []
for tmp in rule:
    s = str(list(tmp[0])[0])+'-->'+str(list(tmp[1])[0])+'---'+str(tmp[2])
    strongRule.append(s)
strongRule_json = json.dumps(strongRule,indent=4)
store(strongRule_json,'/Users/wy/Desktop/strongRule.json')


# In[161]:

strongRule


# In[ ]:

# reference Machine Learning in action


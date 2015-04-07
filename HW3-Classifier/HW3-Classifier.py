
# coding: utf-8

# # data processing

# In[1]:

import pandas as pd


# In[2]:

def loadData(datapath):
    # load data並進行 filter 只保留下面兩種格式的資料，其他有缺項的一律不考慮
    # 第一欄皆為 'M' 是個沒必要的feature故拿掉
    # {0 M,2 Gold,3 85,4 80000}
    # {0 M,       3 49,4 40000}
    with open (datapath,'r') as f:
        lines = f.readlines()
        filterL = []
        for line in lines:
            tfL = line.split()
            try:
                # feature:0
                tf0 = tfL[1].split(',')
                # feature:1
                tf1 = tfL[2].split(',')
                # feature:2
                tf2 = tfL[3].split(',')
                # feature:3
                tf3 = tfL[4].split(',')
                # feature:4
                tf4 = tfL[5].replace('}', '')
                if tf0[1]=='1' and tf1[1]=='2' and tf2[1]=='3' and tf3[1]=='4':
                    filterL.append([float(tf1[0]),tf2[0],float(tf3[0]),float(tf4)])
            except :
                pass

            try:
                # feature:0
                tf0 = tfL[1].split(',')
                # feature:1
                tf1 = tfL[2].split(',')
                # feature:3
                tf2 = tfL[3].split(',')
                # feature:4
                tf3 = tfL[4].replace('}', '')
                if tf0[1]=='1' and tf1[1]=='3' and tf2[1]=='4':
                    filterL.append([float(tf1[0]),'Basic',float(tf2[0]),float(tf3)])
            except :
                pass
    return filterL


# In[3]:

datapath = '/Users/wy/Desktop/data mining/hw3-Q36034188/training.txt'
filterL =  loadData(datapath)


# In[4]:

# rowname = ['marital_status','num_children_at_home','member_card','age','year_income']
rowname = ['num_children_at_home','member_card','age','year_income']
df = pd.DataFrame(filterL, columns=rowname)


# In[5]:

# 簡單看一下data
df.head()


# In[6]:

# 看一下data的大概狀況
df.describe()


# In[7]:

df.max()


# In[8]:

df.mean()


# In[9]:

def numericTra(feature,a,b,c):
    if feature > a:
        return 4
    elif a >= feature > b:
        return 3
    elif b>= feature > c:
        return 2
    elif c>= feature:
        return 1
# 離散化 把numeric分成四個等級 4 > (max+mean/2) > 3 > mean > 2 >(mean+min/2) > 1
def discretization(df,filterL):
#     import copy
#     new_list = copy.deepcopy(filterL)
    # mean
    num_children_at_homeMean = df.mean()[0]
    ageMean = df.mean()[1]
    year_incomeMean = df.mean()[2]
    # (mean＋max)/2
    maxMean = (df.max()+df.mean())/2.
    num_children_at_homeMaxMean = maxMean[2]
    ageMaxMean = maxMean[0]
    year_incomeMaxMean = maxMean[3]
    # (mean＋min)/2
    minMean = (df.mean()+df.min())/2.
    num_children_at_homeMinMean = minMean[2]
    ageMinMean = minMean[0]
    year_incomeMinMean = minMean[3]
    for ind in range(len(filterL)):
        filterL[ind][0] = numericTra(filterL[ind][0],num_children_at_homeMaxMean,num_children_at_homeMean,num_children_at_homeMinMean)
        filterL[ind][2] = numericTra(filterL[ind][2],ageMaxMean,year_incomeMean,ageMinMean)
        filterL[ind][3] = numericTra(filterL[ind][3],year_incomeMaxMean,year_incomeMean,year_incomeMinMean)
discretization(df,filterL)


# In[10]:

# 把 label 移到feature最後
def createDataLabel(filterL):
    dataSet=[]
    for ind in range(len(filterL)):
        dataSet.append([filterL[ind][0],filterL[ind][2],filterL[ind][3],filterL[ind][1]])
    return dataSet


# In[11]:

# 把data前處理包成一個function
def preprocessing(datapath):
    filterL = loadData(datapath)
    rowname = ['num_children_at_home','member_card','age','year_income']
    df = pd.DataFrame(filterL, columns=rowname)
    discretization(df,filterL)
    dataSet = createDataLabel(filterL)
    return dataSet


# In[12]:

datapath = '/Users/wy/Desktop/data mining/hw3-Q36034188/training.txt'
dataSetHw3 = preprocessing(datapath)


# In[13]:

dataSetHw3[:5]


# ---

# # decision tree

# In[14]:

import math
import operator


# In[15]:

def createDataSet():
    dataSet = [['a', 1, 'yes'],
               ['a', 1, 'yes'],
               ['a', 0, 'no'],
               ['b', 1, 'no'],
               ['b', 1, 'no']]
    labels = ['no surfacing','flippers']
    #change to discrete values
    return dataSet, labels


# In[16]:

dataSet, labels = createDataSet()


# In[17]:

# 計算entropy
def calcShannonEnt(dataSet):
    numEntries = len(dataSet)
    labelCounts = {}
    for featVec in dataSet: 
        # 選擇 label
        currentLabel = featVec[-1]
        if currentLabel not in labelCounts.keys(): 
            labelCounts[currentLabel] = 0.
        labelCounts[currentLabel] += 1.
    shannonEnt = 0.
    for key in labelCounts:
        prob = labelCounts[key]/numEntries
        # -P(V1)*LOG2P(V)
        shannonEnt -= prob * math.log(prob,2) #log base 2
    return shannonEnt


# In[18]:

calcShannonEnt(dataSet)


# In[19]:

def splitDataSet(dataSet, axis, value):
    # axis 第幾個feature需要被切割 , value一樣才保留下來
    retDataSet = []
    for featVec in dataSet:
        if featVec[axis] == value:
            # pass the axis
            reducedFeatVec = featVec[:axis]
            reducedFeatVec.extend(featVec[axis+1:])
            retDataSet.append(reducedFeatVec)
    return retDataSet


# In[20]:

splitDataSet(dataSet,1,1)


# In[21]:

splitDataSet(dataSet,1,0)


# In[22]:

dataSet


# In[23]:

def chooseBestFeatureToSplit(dataSet):
    # 最後一欄為label
    numFeatures = len(dataSet[0]) - 1
    # 目前的baseEntropy
    baseEntropy = calcShannonEnt(dataSet)
    bestInfoGain = 0.0
    bestFeature = -1
    for i in range(numFeatures):        #iterate over all the features
        # 同欄的feature併到同list
        featList = [example[i] for example in dataSet]
        # 取得該同list的feature集合
        uniqueVals = set(featList)       #get a set of unique values
        newEntropy = 0.0
        for value in uniqueVals:
            subDataSet = splitDataSet(dataSet, i, value)
            prob = len(subDataSet)/float(len(dataSet))
            newEntropy += prob * calcShannonEnt(subDataSet)     
        infoGain = baseEntropy - newEntropy     #calculate the info gain; ie reduction in entropy
        if (infoGain > bestInfoGain):       #compare this to the best gain so far
            bestInfoGain = infoGain         #if better than current best, set to best
            bestFeature = i
    return bestFeature                      #returns an integer


# In[24]:

bestFeature = chooseBestFeatureToSplit(dataSet)


# In[25]:

bestFeature


# In[26]:

def majorityCnt(classList):
    classCount={}
    for vote in classList:
        if vote not in classCount.keys(): classCount[vote] = 0
        classCount[vote] += 1
    sortedClassCount = sorted(classCount.iteritems(), key=operator.itemgetter(1), reverse=True)
    return sortedClassCount[0][0]

def createTree(dataSet,labels):
    # 把dataSet的labels 存進 classList
    classList = [example[-1] for example in dataSet]
    # 當labels一樣時停止
    if classList.count(classList[0]) == len(classList):
        return classList[0]
    
    # 當沒有feature可以劃分時停止
    if len(dataSet[0]) == 1: 
        # 挑選次數最多的label當作return value
        return majorityCnt(classList)
    # 選擇最佳切割的feature
    bestFeat = chooseBestFeatureToSplit(dataSet)
    bestFeatLabel = labels[bestFeat]
    myTree = {bestFeatLabel:{}}
    del(labels[bestFeat])
    featValues = [example[bestFeat] for example in dataSet]
    uniqueVals = set(featValues)
    for value in uniqueVals:
        subLabels = labels[:]       #copy all of labels, so trees don't mess up existing labels
        myTree[bestFeatLabel][value] = createTree(splitDataSet(dataSet, bestFeat, value),subLabels)
    return myTree   


# In[27]:

myTree = createTree(dataSet,labels)


# In[28]:

myTree


# In[29]:

def classify(inputTree,featLabels,testVec):
    firstStr = inputTree.keys()[0]
    secondDict = inputTree[firstStr]
    featIndex = featLabels.index(firstStr)
    key = testVec[featIndex]
    valueOfFeat = secondDict[key]
    if isinstance(valueOfFeat, dict): 
        classLabel = classify(valueOfFeat, featLabels, testVec)
    else: classLabel = valueOfFeat
    return classLabel


# In[30]:

dataSet, labels = createDataSet()


# In[31]:

classify(myTree,labels,['a',0])


# ---
# # load hw3 data

# In[32]:

dataSetHw3[:5]


# In[33]:

labelsHw3 = ['num_children_at_home','age','year_income']


# In[34]:

myTreeHw3 = createTree(dataSetHw3,labelsHw3)


# In[35]:

# tree model 建立完成
myTreeHw3


# In[36]:

# 載入 test data
testdatapath = '/Users/wy/Desktop/data mining/hw3-Q36034188/test.txt'
dataSetTestHw3 = preprocessing(datapath)


# In[37]:

testFeature=[]
testAnswer=[]
for line in range(len(dataSetTestHw3)):
    testFeature.append(dataSetTestHw3[line][:3])
    testAnswer.append(dataSetTestHw3[line][-1])


# In[38]:

testFeature[:5]


# In[39]:

testAnswer[:5]


# In[40]:

dataSetTestHw3[:5]


# In[41]:

labelsHw3 = ['num_children_at_home','age','year_income']


# In[42]:

classificationsAnswer=[]
for f in testFeature:
    try:
        classificationsAnswer.append(classify(myTreeHw3,labelsHw3,f))
    except:
        classificationsAnswer.append('Basic')


# In[43]:

num = len(testAnswer)
ans = 0
for a,b in zip(classificationsAnswer,testAnswer):
    if a==b:
        ans+=1
correctness = float(ans)/float(num)


# In[44]:

# 準確率 0.56%
correctness


# In[45]:

# 建立真實和分類器結果的table
GGold=0
GSilver=0
GNormal=0
GBasic=0

SGold=0
SSilver=0
SNormal=0
SBasic=0

NGold=0
NSilver=0
NNormal=0
NBasic=0

BGold=0
BSilver=0
BNormal=0
BBasic=0
for a,b in zip(classificationsAnswer,testAnswer):
    if b=='Gold':
        if a=='Gold':
            GGold+=1
        elif a=='Silver':
            GSilver+=1
        elif a=='Normal':
            GNormal+=1
        elif a=='Basic':
            GBasic+=1
    elif b=='Silver':
        if a=='Gold':
            SGold+=1
        elif a=='Silver':
            SSilver+=1
        elif a=='Normal':
            SNormal+=1
        elif a=='Basic':
            SBasic+=1       
    elif b=='Normal':
        if a=='Gold':
            NGold+=1
        elif a=='Silver':
            NSilver+=1
        elif a=='Normal':
            NNormal+=1
        elif a=='Basic':
            NBasic+=1         
    elif b=='Basic':
        if a=='Gold':
            BGold+=1
        elif a=='Silver':
            BSilver+=1
        elif a=='Normal':
            BNormal+=1
        elif a=='Basic':
            BBasic+=1  
corMatrix = [[GGold,GSilver,GNormal,GBasic],[SGold,SSilver,SNormal,SBasic],[NGold,NSilver,NNormal,NBasic],[BGold,BSilver,BNormal,BBasic]]
rowname = ['Gold','Silver','Normal','Basic']
indexname = ['True-Gold','True-Silver','True-Normal','True-Basic']
corMatrixdf = pd.DataFrame(corMatrix, index=indexname,columns=rowname)


# In[46]:

corMatrixdf


# In[ ]:




import h2o_cmd, h2o
import re, random, math

def plotLists(xList, xLabel=None, eListTitle=None, eList=None, eLabel=None, fListTitle=None, fList=None, fLabel=None, server=False):
    if h2o.python_username!='kevin':
        return

    # Force matplotlib to not use any Xwindows backend.
    if server:
        import matplotlib
        matplotlib.use('Agg')

    import pylab as plt
    print "xList", xList
    print "eList", eList
    print "fList", fList

    font = {'family' : 'normal',
            'weight' : 'normal',
            'size'   : 26}
    ### plt.rc('font', **font)
    plt.rcdefaults()


    if eList:
        if eListTitle:
            plt.title(eListTitle)
        plt.figure()
        plt.plot (xList, eList)
        plt.xlabel(xLabel)
        plt.ylabel(eLabel)
        plt.draw()
        plt.savefig('eplot.jpg',format='jpg')
        # Image.open('testplot.jpg').save('eplot.jpg','JPEG')

    if fList:
        if fListTitle:
            plt.title(fListTitle)
        plt.figure()
        plt.plot (xList, fList)
        plt.xlabel(xLabel)
        plt.ylabel(fLabel)
        plt.draw()
        plt.savefig('fplot.jpg',format='jpg')
        # Image.open('fplot.jpg').save('fplot.jpg','JPEG')

    if eList or fList:
        plt.show()


# pretty print a cm that the C
def pp_cm(jcm, header=None):
    # header = jcm['header']
    # hack col index header for now..where do we get it?
    header = ['"%s"'%i for i in range(len(jcm[0]))]
    # cm = '   '.join(header)
    cm = '{0:<8}'.format('')
    for h in header: 
        cm = '{0}|{1:<8}'.format(cm, h)

    cm = '{0}|{1:<8}'.format(cm, 'error')
    c = 0
    for line in jcm:
        lineSum  = sum(line)
        if c < 0 or c >= len(line):
            raise Exception("Error in h2o_gbm.pp_cm. c: %s line: %s len(line): %s jcm: %s" % (c, line, len(line), h2o.dump_json(jcm)))
        print "c:", c, "line:", line
        errorSum = lineSum - line[c]
        if (lineSum>0):
            err = float(errorSum) / lineSum
        else:
            err = 0.0
        fl = '{0:<8}'.format(header[c])
        for num in line: fl = '{0}|{1:<8}'.format(fl, num)
        fl = '{0}|{1:<8.2f}'.format(fl, err)
        cm = "{0}\n{1}".format(cm, fl)
        c += 1
    return cm

def pp_cm_summary(cm):
    # hack cut and past for now (should be in h2o_gbm.py?
    scoresList = cm
    totalScores = 0
    totalRight = 0
    # individual scores can be all 0 if nothing for that output class
    # due to sampling
    classErrorPctList = []
    predictedClassDict = {} # may be missing some? so need a dict?
    for classIndex,s in enumerate(scoresList):
        classSum = sum(s)
        if classSum == 0 :
            # why would the number of scores for a class be 0? 
            # in any case, tolerate. (it shows up in test.py on poker100)
            print "classIndex:", classIndex, "classSum", classSum, "<- why 0?"
        else:
            if classIndex >= len(s):
                print "Why is classindex:", classIndex, 'for s:"', s
            else:
            # H2O should really give me this since it's in the browser, but it doesn't
                classRightPct = ((s[classIndex] + 0.0)/classSum) * 100
                totalRight += s[classIndex]
                classErrorPct = 100 - classRightPct
                classErrorPctList.append(classErrorPct)
                ### print "s:", s, "classIndex:", classIndex
                print "class:", classIndex, "classSum", classSum, "classErrorPct:", "%4.2f" % classErrorPct

                # gather info for prediction summary
                for pIndex,p in enumerate(s):
                    if pIndex not in predictedClassDict:
                        predictedClassDict[pIndex] = p
                    else:
                        predictedClassDict[pIndex] += p

        totalScores += classSum

    print "Predicted summary:"
    # FIX! Not sure why we weren't working with a list..hack with dict for now
    for predictedClass,p in predictedClassDict.items():
        print str(predictedClass)+":", p

    # this should equal the num rows in the dataset if full scoring? (minus any NAs)
    print "totalScores:", totalScores
    print "totalRight:", totalRight
    if totalScores != 0:  pctRight = 100.0 * totalRight/totalScores
    else: pctRight = 0.0
    print "pctRight:", "%5.2f" % pctRight
    pctWrong = 100 - pctRight
    print "pctWrong:", "%5.2f" % pctWrong

    return pctWrong


# I just copied and changed GBM to GBM. Have to update to match GBM params and responses

def pickRandGbmParams(paramDict, params):
    colX = 0
    randomGroupSize = random.randint(1,len(paramDict))
    for i in range(randomGroupSize):
        randomKey = random.choice(paramDict.keys())
        randomV = paramDict[randomKey]
        randomValue = random.choice(randomV)
        params[randomKey] = randomValue


# compare this glm to last one. since the files are concatenations, 
# the results should be similar? 10% of first is allowed delta
def compareToFirstGbm(self, key, glm, firstglm):
    # if isinstance(firstglm[key], list):
    # in case it's not a list allready (err is a list)
    h2o.verboseprint("compareToFirstGbm key:", key)
    h2o.verboseprint("compareToFirstGbm glm[key]:", glm[key])
    # key could be a list or not. if a list, don't want to create list of that list
    # so use extend on an empty list. covers all cases?
    if type(glm[key]) is list:
        kList  = glm[key]
        firstkList = firstglm[key]
    elif type(glm[key]) is dict:
        raise Exception("compareToFirstGLm: Not expecting dict for " + key)
    else:
        kList  = [glm[key]]
        firstkList = [firstglm[key]]

    for k, firstk in zip(kList, firstkList):
        # delta must be a positive number ?
        delta = .1 * abs(float(firstk))
        msg = "Too large a delta (" + str(delta) + ") comparing current and first for: " + key
        self.assertAlmostEqual(float(k), float(firstk), delta=delta, msg=msg)
        self.assertGreaterEqual(abs(float(k)), 0.0, str(k) + " abs not >= 0.0 in current")


def goodXFromColumnInfo(y, 
    num_cols=None, missingValuesDict=None, constantValuesDict=None, enumSizeDict=None, 
    colTypeDict=None, colNameDict=None, keepPattern=None, key=None, 
    timeoutSecs=120, forRF=False, noPrint=False):

    y = str(y)

    # if we pass a key, means we want to get the info ourselves here
    if key is not None:
        (missingValuesDict, constantValuesDict, enumSizeDict, colTypeDict, colNameDict) = \
            h2o_cmd.columnInfoFromInspect(key, exceptionOnMissingValues=False, 
            max_column_display=99999999, timeoutSecs=timeoutSecs)
        num_cols = len(colNameDict)

    # now remove any whose names don't match the required keepPattern
    if keepPattern is not None:
        keepX = re.compile(keepPattern)
    else:
        keepX = None

    x = range(num_cols)
    # need to walk over a copy, cause we change x
    xOrig = x[:]
    ignore_x = [] # for use by RF
    for k in xOrig:
        name = colNameDict[k]
        # remove it if it has the same name as the y output
        if str(k)== y: # if they pass the col index as y
            if not noPrint:
                print "Removing %d because name: %s matches output %s" % (k, str(k), y)
            x.remove(k)
            # rf doesn't want it in ignore list
            # ignore_x.append(k)
        elif name == y: # if they pass the name as y 
            if not noPrint:
                print "Removing %d because name: %s matches output %s" % (k, name, y)
            x.remove(k)
            # rf doesn't want it in ignore list
            # ignore_x.append(k)

        elif keepX is not None and not keepX.match(name):
            if not noPrint:
                print "Removing %d because name: %s doesn't match desired keepPattern %s" % (k, name, keepPattern)
            x.remove(k)
            ignore_x.append(k)

        # missing values reports as constant also. so do missing first.
        # remove all cols with missing values
        # could change it against num_rows for a ratio
        elif k in missingValuesDict:
            value = missingValuesDict[k]
            if not noPrint:
                print "Removing %d with name: %s because it has %d missing values" % (k, name, value)
            x.remove(k)
            ignore_x.append(k)

        elif k in constantValuesDict:
            value = constantValuesDict[k]
            if not noPrint:
                print "Removing %d with name: %s because it has constant value: %s " % (k, name, str(value))
            x.remove(k)
            ignore_x.append(k)

        # this is extra pruning..
        # remove all cols with enums, if not already removed
        elif k in enumSizeDict:
            value = enumSizeDict[k]
            if not noPrint:
                print "Removing %d %s because it has enums of size: %d" % (k, name, value)
            x.remove(k)
            ignore_x.append(k)

    if not noPrint:
        print "x has", len(x), "cols"
        print "ignore_x has", len(ignore_x), "cols"
    x = ",".join(map(str,x))
    ignore_x = ",".join(map(str,ignore_x))

    if not noPrint:
        print "\nx:", x
        print "\nignore_x:", ignore_x

    if forRF:
        return ignore_x
    else:
        return x


def showGBMGridResults(GBMResult, expectedErrorMax, classification=True):
    # print "GBMResult:", h2o.dump_json(GBMResult)
    jobs = GBMResult['jobs']
    print "GBM jobs:", jobs
    for jobnum, j in enumerate(jobs):
        _distribution = j['_distribution']
        model_key = j['destination_key']
        job_key = j['job_key']
        # inspect = h2o_cmd.runInspect(key=model_key)
        # print "jobnum:", jobnum, h2o.dump_json(inspect)
        gbmTrainView = h2o_cmd.runGBMView(model_key=model_key)
        print "jobnum:", jobnum, h2o.dump_json(gbmTrainView)

        if classification:
            cms = gbmTrainView['gbm_model']['cms']
            cm = cms[-1]['_arr'] # take the last one
            print "GBM cms[-1]['_predErr']:", cms[-1]['_predErr']
            print "GBM cms[-1]['_classErr']:", cms[-1]['_classErr']

            pctWrongTrain = pp_cm_summary(cm);
            if pctWrongTrain > expectedErrorMax:
                raise Exception("Should have < %s error here. pctWrongTrain: %s" % (expectedErrorMax, pctWrongTrain))

            errsLast = gbmTrainView['gbm_model']['errs'][-1]
            print "\nTrain", jobnum, job_key, "\n==========\n", "pctWrongTrain:", pctWrongTrain, "errsLast:", errsLast
            print "GBM 'errsLast'", errsLast
            print pp_cm(cm)
        else:
            print "\nTrain", jobnum, job_key, "\n==========\n", "errsLast:", errsLast
            print "GBMTrainView errs:", gbmTrainView['gbm_model']['errs']



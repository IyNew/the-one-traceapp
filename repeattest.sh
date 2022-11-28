settingpath=./settingfiles
settingBase=./test_base.txt
# pace=0.2
# interval=100
# numofnodes=500
endtime=20000
TARate=0.1
pace=0.1
# istracing=false
# numofTA=1
isreshare=false
# interval=50

# for pace in 0.2 0.4 0.6 0.8 1.0
for numofnodesTotal in 200 
do
    for interval in 100
    do
        # for numofnodesTotal in 200 300 400 500
        for reportProb in 0.1 0.25 0.5 1.0
        do
            for repeatTimes in 0 1 2 3
            do
                numofTA=$(echo $numofnodesTotal*$TARate | bc)
                # echo "numofTA=$numofTA"
                numofnodes=$(echo $numofnodesTotal-$numofTA | bc)
                # echo "numofnodes=$numofnodes"
                for istracing in 3
                do
                    for isreshare in false
                    do
                        
                        testname="RepeatTest-"$numofnodes"nodes-"$numofTA"TA-"$interval"-"$pace"-"$istracing"tracemode-"$repeatTimes"repeat-"$reportProb"reportRate-"$endtime
                        reportname="./reports/"$testname"_TraceAppReporter.txt"
                        if test -f "$reportname"; then
                            # echo "$testname exists."
                            echo $testname" completed."
                        else
                            echo $testname
                            # copy ./settings/test.txt to ./settings/testname.txt
                            cp $settingBase $settingpath/$testname.txt
                            # replace the values in the file
                            sed -i.bk "s/Testbase/$testname/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tnumofnodes/$numofnodes/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tendtime/$endtime/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tinterval/$interval/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tpace/$pace/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tistracing/$istracing/g" $settingpath/$testname.txt
                            sed -i.bk "s/TnumofTA/$numofTA/g" $settingpath/$testname.txt
                            sed -i.bk "s/Tisreshare/$isreshare/g" $settingpath/$testname.txt
                            sed -i.bk "s/Treportprob/$reportProb/g" $settingpath/$testname.txt
                            sed -i.bk "s/Trepeattimes/$repeatTimes/g" $settingpath/$testname.txt


                            rm $settingpath/$testname.txt.bk

                            # clear the setting files with arg "clear"
                            if [ "$1" = "clear" ]; then
                                rm $settingpath/$testname.txt
                            fi
                            ./one.sh -b 1 $settingpath/$testname.txt
                        fi
                        
                        
                    done
                    
                done
            done
        done
    done
done

# curl -X GET https://api.day.app/i5xtQapPZVuE2gfQLvbZD4/completed
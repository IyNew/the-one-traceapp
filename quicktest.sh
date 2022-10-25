settingpath=./settingfiles
# pace=0.2
# interval=100
# numofnodes=500
endtime=5000
# istracing=false
# numofTA=1
# isreshare=false
# interval=50

for pace in 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
do
    for interval in 10
    do
        for numofnodesTotal in 200 300 400 500
        do
            for TARate in 0.05
            do
                numofTA=$(echo $numofnodesTotal*$TARate | bc)
                echo "numofTA=$numofTA"
                numofnodes=$(echo $numofnodesTotal-$numofTA | bc)
                echo "numofnodes=$numofnodes"
                for istracing in 0 1 2 3
                do
                    for isreshare in true false
                    do
                        testname="Test-"$numofnodes"nodes-"$numofTA"TA-"$interval"-"$pace"-"$istracing"tracemode-"$isreshare"reshare"
                        echo $testname
                        # copy ./settings/test.txt to ./settings/testname.txt
                        cp $settingpath/test.txt $settingpath/$testname.txt
                        # replace the values in the file
                        sed -i.bk "s/Testbase/$testname/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tnumofnodes/$numofnodes/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tendtime/$endtime/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tinterval/$interval/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tpace/$pace/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tistracing/$istracing/g" $settingpath/$testname.txt
                        sed -i.bk "s/TnumofTA/$numofTA/g" $settingpath/$testname.txt
                        sed -i.bk "s/Tisreshare/$isreshare/g" $settingpath/$testname.txt

                        rm $settingpath/$testname.txt.bk

                        # clear the setting files with arg "clear"
                        if [ "$1" = "clear" ]; then
                            rm $settingpath/$testname.txt
                        fi
                        # ./one.sh $settingpath/$testname.txt
                    done
                    
                done
            done
        done
    done
done

# curl -X GET https://api.day.app/i5xtQapPZVuE2gfQLvbZD4/completed
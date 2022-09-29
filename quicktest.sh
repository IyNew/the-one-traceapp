settingpath=./settingfiles
# pace=0.2
# interval=100
# numofnodes=500
endtime=500
# istracing=false
# numofTA=1

for pace in 1.0
do
    for interval in 50 
    do
        for numofnodes in 250
        do
            for numofTA in 50
            do
                for istracing in 0 1 2
                do
                    testname="Test-"$numofnodes"nodes-"$numofTA"TA-"$endtime"end-"$interval"-"$pace"-"$istracing"tracemode"
                    echo $testname
                    # copy ./settings/test.txt to ./settings/testname.txt
                    cp $settingpath/test.txt $settingpath/$testname.txt
                    # replace the values in the file
                    sed -i.bk "s/Testbase/$testname/g" $settingpath/$testname.txt
                    sed -i.bk "s/Tnumofnodes/$numofnodes/g" $settingpath/$testname.txt
                    # sed -i.bk "s/Group2.nrofHosts = 500/Group2.nrofHosts = $numofnodes/g" $settingpath/$testname.txt
                    sed -i.bk "s/Tendtime/$endtime/g" $settingpath/$testname.txt
                    sed -i.bk "s/Tinterval/$interval/g" $settingpath/$testname.txt
                    sed -i.bk "s/Tpace/$pace/g" $settingpath/$testname.txt
                    sed -i.bk "s/Tistracing/$istracing/g" $settingpath/$testname.txt
                    sed -i.bk "s/TnumofTA/$numofTA/g" $settingpath/$testname.txt

                    rm $settingpath/$testname.txt.bk

                    ./one.sh -b 1 $settingpath/$testname.txt
                done
            done
        done
    done
done



curl -X GET https://api.day.app/i5xtQapPZVuE2gfQLvbZD4/completed
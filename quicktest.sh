settingpath=./settingfiles
pace=0.2
interval=100
numofnodes=500
endtime=1000
istracing=false
testname="Test-"$numofnodes"-"$endtime"-"$interval"-"$pace"-"$istracing
echo $testname
# copy ./settings/test.txt to ./settings/testname.txt
cp $settingpath/test.txt $settingpath/$testname.txt
# replace the values in the file
sed -i.bk "s/name = Testbase/name = $testname/g" $settingpath/$testname.txt
sed -i.bk "s/numofnodes = 500/numofnodes = $numofnodes/g" $settingpath/$testname.txt
sed -i.bk "s/Group2.nrofHosts = 500/Group2.nrofHosts = $numofnodes/g" $settingpath/$testname.txt
sed -i.bk "s/endtime = 2000/endtime = $endtime/g" $settingpath/$testname.txt
sed -i.bk "s/interval = 100/interval = $interval/g" $settingpath/$testname.txt
sed -i.bk "s/pace = 0.1/pace = $pace/g" $settingpath/$testname.txt
sed -i.bk "s/istracing = false/istracing = $istracing/g" $settingpath/$testname.txt

./one.sh -b 1 $settingpath/$testname.txt

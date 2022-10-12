a=10
b=0.1
# c=a*b
c=$(echo $a*$b | bc)
echo $c
# d=a-c
d=$(echo $a-$c | bc)
echo $d
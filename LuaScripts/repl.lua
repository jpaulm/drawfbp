-- repl.lua

i = dfselct("OUT");   -- get element count for port "OUT"

value, IPaddr, size, type = dfsrecv("IN")  

while value == 0 do 
string = dfsderef(IPaddr)
   for j = 0, i - 1 do
       value, IPaddr2 = dfscrep(string)
	   --myprint(IPaddr2)
       value = dfssend("OUT["..j.."]", IPaddr2) 
   end
   --myprint(IPaddr)
   value = dfsdrop(IPaddr)

   value, IPaddr, size, type = dfsrecv("IN")
end
return 0
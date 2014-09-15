
i = dfselct("IN");   -- get element count for port "IN"

for j = 0, i - 1 do
  value, IPaddr, size, type = dfsrecv("IN["..j.."]")    -- receive from element j of port "IN"
  while value == 0 do  
  --myprint(IPaddr)
     value = dfssend("OUT", IPaddr)                     -- send IP to port "OUT"
     value, IPaddr, size, type = dfsrecv("IN["..j.."]") -- receive from element j of port "IN"
  end
end
return 0
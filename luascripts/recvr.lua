-- Experimental non-looper

--count = 0
value, IPaddr, size, type = dfsrecv("IN[0]")
--while value == 0 do  
   --myprint(IPaddr)
  -- count = count + 1
   value = dfsdrop(IPaddr)
 --  value, IPaddr, size, type = dfsrecv("IN")
--end
--print(count)
return 0


-- TryLua.cpp is basically 

--        Gen1 -> Concat -> Repl -> Drop, 
--        Gen2 -> Concat


-- Elapsed time -  54 secs

-- so that's 1,000,000 each of "Lua" create, drop
--           3,000,000 each of send, receive

-- Replacing drop.lua by recvr.lua - does the same as drop.lua, but as a non-looper, 
--   takes approx. 10 mins.! 

value, IPaddr, size, type = dfsrecv("COUNT")

i = dfsderef(IPaddr)
dfsdrop(IPaddr)
print (i)

for j = 1, i do
  value, IPaddr = dfscrep("string"..j)
  --myprint(IPaddr)
  value = dfssend("OUT", IPaddr)
end
return 0
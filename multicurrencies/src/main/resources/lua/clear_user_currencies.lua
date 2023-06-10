local call = redis.call
local ipairs = ipairs

local userId = KEYS[1]

local key = "currency_user:" .. userId .. ":currencies"

local currencies = call("HGETALL", key)

local k = nil

for _, obj in ipairs(currencies) do
    if k == nil then
        k = obj
        call("HSET", key, k, "0.0")
    else
        k = nil
    end
end

return 1
local call = redis.call

local currencyId = KEYS[1]
local action = KEYS[2]
local uuid = ARGV[1]
local value = ARGV[2]

local key = "currency_user:"

local users = call("SMEMBERS", "currency_users")

for _, userId in ipairs(users) do
    local localKey = key .. userId

    local uuid1 = call("HGET", localKey, "uuid")
    if uuid1 == uuid then
        key = localKey .. ":currencies"
        break
    end
end

if key == "currency_user:" then
    return 0
end

if call("HEXISTS", key, currencyId) == 1 then

    if action == "add" then
        local balance = call("HGET", key, currencyId)
        call("HSET", key, currencyId, balance + value)
        return 1

    elseif action == "remove" then
        local balance = call("HGET", key, currencyId)
        if balance >= value then
            call("HSET", key, currencyId, balance - value)
            return 1
        else
            return 0
        end

    elseif action == "set" then
        call("HSET", key, currencyId, value)
        return 1
    end

else
    if action == "add" or action == "set" then
        call("HSET", key, currencyId, value)
        return 1
    end
end

return 0;
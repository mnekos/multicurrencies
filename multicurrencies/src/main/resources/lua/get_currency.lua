local call = redis.call
local ipairs = ipairs

local key = "currency:"

local table = {}

if #KEYS > 0 then
    local id = KEYS[1]
    key = key .. id

    if not call("EXISTS", key) then
        return table
    end

    table[#table + 1] = id
    table[#table + 1] = call("HGET", key, "name")
    table[#table + 1] = call("HGET", key, "displayname")
    table[#table + 1] = call("HGET", key, "freeflow")
    table[#table + 1] = call("HGET", key, "commandname")
elseif #ARGV > 0 then
    local name = ARGV[1]

    local currencies = call("SMEMBERS", "currencies")
    for _, currencyId in ipairs(currencies) do
        if call("HGET", "currency:" .. currencyId, "name") == name then
            key = key .. currencyId

            table[#table + 1] = currencyId
            table[#table + 1] = call("HGET", key, "name")
            table[#table + 1] = call("HGET", key, "displayname")
            table[#table + 1] = call("HGET", key, "freeflow")
            table[#table + 1] = call("HGET", key, "commandname")
            break
        end
    end
else
    local currencies = call("SMEMBERS", "currencies")
    for _, currencyId in ipairs(currencies) do
        local localKey = key .. currencyId

        table[#table + 1] = currencyId
        table[#table + 1] = call("HGET", localKey, "name")
        table[#table + 1] = call("HGET", localKey, "displayname")
        table[#table + 1] = call("HGET", localKey, "freeflow")
        table[#table + 1] = call("HGET", localKey, "commandname")
    end
end

return table
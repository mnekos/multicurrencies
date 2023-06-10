local call = redis.call
local ipairs = ipairs

local servers = call("HGETALL", "multicurrencies:heartbeats")

local time = tonumber(KEYS[1])

local server = nil

local online = {}

for _, obj in ipairs(servers) do
    if server == nil then
        server = obj
    else
        if obj + 15000 > time then
            local players = call("SMEMBERS", "multicurrencies:server:" .. server)

            for _, player in ipairs(players) do
                online[#online + 1] = player
            end
        end
        server = nil
    end
end

function table.contains(table, element)
    for _, value in pairs(table) do
        if value == element then
            return true
        end
    end
    return false
end

local users = call("SMEMBERS", "currency_users")

local counter = 0

local toDelete = {}

for _, user in ipairs(users) do

    local uuid = call("HGET", "currency_user:" .. user, "uuid")
    if not table.contains(online, uuid) then
        toDelete[#toDelete + 1] = user
        counter = counter + 1
    end
end

for _, user in ipairs(toDelete) do
    call("SREM", "currency_users", user)
    call("DEL", "currency_user:" .. user)
end

return counter
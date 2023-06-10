local call = redis.call
local ipairs = ipairs

local userKey = "currency_user:"

local table = {}

if #KEYS > 0 then
    local id = KEYS[1]
    userKey = userKey .. id

    if not call("EXISTS", userKey) == 1 then
        return table
    end

    table[#table + 1] = id
    table[#table + 1] = call("HGET", userKey, "uuid")
    table[#table + 1] = call("HGET", userKey, "displayname")

    local currenciesKey = userKey .. ":currencies"
    local currencies = call("HGETALL", currenciesKey)

    local key = nil

    for _, obj in ipairs(currencies) do
        if key == nil then
            key = obj
        else
            if call("EXISTS", "currency:" .. key) == 1 then
                table[#table + 1] = key .. ":" .. obj
            end
            key = nil
        end
    end
elseif #ARGV > 1 then
    local mode = ARGV[1]
    local identificator = ARGV[2]

    local users = call("SMEMBERS", "currency_users")

    if mode == "uuid" then
        for _, userId in ipairs(users) do
            local localKey = userKey .. userId

            local uuid = call("HGET", localKey, "uuid")
            if uuid == identificator then
                table[#table + 1] = userId
                table[#table + 1] = uuid
                table[#table + 1] = call("HGET", localKey, "displayname")

                local currenciesKey = localKey .. ":currencies"
                local currencies = call("HGETALL", currenciesKey)

                local key = nil

                for _, obj in ipairs(currencies) do
                    if key == nil then
                        key = obj
                    else
                        if call("EXISTS", "currency:" .. key) == 1 then
                            table[#table + 1] = key .. ":" .. obj
                        end
                        key = nil
                    end
                end

                return table
            end
        end
    elseif mode == "name" then
        identificator = identificator:lower()
        for _, userId in ipairs(users) do
            local localKey = userKey .. userId

            local displayname = call("HGET", localKey, "displayname")
            if displayname:lower() == identificator then
                table[#table + 1] = userId
                table[#table + 1] = call("HGET", localKey, "uuid")
                table[#table + 1] = displayname

                local currenciesKey = localKey .. ":currencies"
                local currencies = call("HGETALL", currenciesKey)

                local key = nil

                for _, obj in ipairs(currencies) do
                    if key == nil then
                        key = obj
                    else
                        if call("EXISTS", "currency:" .. key) == 1 then
                            table[#table + 1] = key .. ":" .. obj
                        end
                        key = nil
                    end
                end

                return table
            end
        end
    end
else
    local users = call("SMEMBERS", "currency_users")

    for _, userId in ipairs(users) do
        local localKey = key .. userId

        table[#table + 1] = userId
        table[#table + 1] = call("HGET", localKey, "uuid")
        table[#table + 1] = call("HGET", localKey, "displayname")

        local currenciesKey = localKey .. ":currencies"
        local currencies = call("HGETALL", currenciesKey)

        local key = nil

        for _, obj in ipairs(currencies) do
            if key == nil then
                key = obj
            else
                if call("EXISTS", "currency:" .. key) == 1 then
                    table[#table + 1] = key .. ":" .. obj
                end
                key = nil
            end
        end
    end
end

return table
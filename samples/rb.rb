require 'sinatra'

set :port, 8084
set :bind, "45.56.85.191"

get '/' do
  "Hello team #{Random.rand(10000)}"
end

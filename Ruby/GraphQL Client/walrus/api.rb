## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require 'net/http'
require 'uri'
require 'json'

module Walrus
    def request(data)
      uri = URI.parse($config.get("api", "url"))

      params = {query: data}
      params = Hash[URI.decode_www_form(uri.query || '')].merge(params)
      uri.query = URI.encode_www_form(params)

      header = {'Content-Type': 'text/json'}
      http = Net::HTTP.new(uri.host, uri.port)
      request = Net::HTTP::Post.new(uri.request_uri, header)

      # Send the request
      response = http.request(request)
      res = JSON.parse(response.body, symbolize_names: true)
      puts res
      unless res[:errors].nil?
        errors = res[:errors]
        message = "#{errors.size} error(s) while executing query: "
        errors.each do |error|
          message += "\"#{error[:message]}\", "
        end
        message = message.delete_suffix(", ")
        raise message
      end
      return res[:data]
    end
end

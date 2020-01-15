## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require "yaml"
require "kubeclient"

module Templates
  class ResourceTemplate
    def initialize(type, name)
      @path = "resources/templates/#{type}/#{name}.yml"
      raise "Template \"#{@path}\" not found!" unless File.file?(@path)
    end

    def pass_to_cluster(cluster, resource)
      raise NotImplementedError, "must specify a way to create the resource"
    end

    def create(env, cluster)
      raw = File.read(@path)
      # Replace placeholders
      raw.gsub!(/\$\{([ a-zA-Z0-9_-]{1,})\b\}|\$([a-zA-Z0-9_-]{1,})\b/) do |var|
          clean = var.gsub(/[\{\}\$]/, "")
          match = env[:"#{clean}"]
          raise "Unable to find #{clean} in environment!" if match.nil?
          match
      end
      # Load YML
      parsed = YAML.load(raw)
      pass_to_cluster(cluster, Kubeclient::Resource.new(parsed))
    end
  end
end

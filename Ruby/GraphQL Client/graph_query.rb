## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

class GraphQuery
  def initialize(name)
    @path = "resources/queries/#{name}.json"
    raise "Query \"#{@path}\" not found!" unless File.file?(@path)
  end

  def create(env)
    raw = File.read(@path)
    # Replace placeholders
    raw.gsub!(/\$\{([ a-zA-Z0-9_-]{1,})\b\}|\$([a-zA-Z0-9_-]{1,})\b/) do |var|
        clean = var.gsub(/[\{\}\$]/, "")
        match = env[:"#{clean}"]
        raise "Unable to find #{clean} in environment!" if match.nil?
        match
    end
    raw
      .gsub(/(^.*.#.+)/, '') # Remove comments not at first index
      .gsub(/(^#.+)/, '') # Remove comments at first index
      .gsub("\n", "") # Remove newlines
      .gsub(/\s+/, "") # Remove all spaces
  end
end

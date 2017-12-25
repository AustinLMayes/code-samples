require 'nokogiri'

# Get a human sting from an XML
def localized_sting(str)
  ret = ''
  str.scan(/(?<={)[^}]*(?=})/).each do |trans|
    doc = Nokogiri::XML(File.open($avicus['maps-path'] + '/Shared/locales/en_US.xml'))
    ret << doc.css('map locales locale ' + trans.gsub('.', ' ')).text + ' '
  end
  ret[0...ret.length-1]
end

# Convert a bukkit chat color to an HTML color.
def color_to_html(color)
  case color.downcase
    when 'aqua'
      return '#00E8D5'
    when 'cyan'
      return '#00B0A2'
    when 'gray'
      return '#6C6C6C'
    when 'red'
      return '#DC0000'
    when 'blue'
      return '#3000DC'
    when 'green'
      return '#007F17'
    when 'lime'
      return '#00DA28'
    when 'yellow'
      return '#ffea00'
    when 'orange'
      return '#EC9200'
    when 'purple'
      return '#C500EC'
    when 'pink'
      return '#EC00D8'
    else
      return 'none'
  end
end

require 'xml/map_xml.rb'
require 'find'

# Get all maps in a directory, and search recursively.
def all_maps(start = $avicus['maps-path'])
  loaded = {}
  Find.find(start) do |path|
    name = File.basename(path)
    if FileTest.directory?(path)
      if $avicus['ignored-map-directories'].include?(name)
        Find.prune
      else
        next
      end
    else
      if name == 'map.xml'
        begin
          m = MapXml.new(path)
          loaded[m.slug] = m
        rescue => e
          puts "Failed to load map! (#{e}) at #{path}"
        end
      end
    end
  end
  return loaded.sort_by { |name, map| name.downcase }.to_h
end

# Sort maps into categories based on their location in the maps directory,
def categorize_maps(maps = [])
  categorized = {}
  maps.each do |m|
    categorized[m.category] = [] if categorized[m.category].nil?
    categorized[m.category] << m
  end
  categorized
end

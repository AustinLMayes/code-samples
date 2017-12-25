require 'nokogiri'
require 'xml/parsing_utils.rb'

# Represents an XML file for an atlas map.
class MapXml
  attr_reader :path

  include Cachable

  def initialize(path)
    @path = path
    @path_remote = path.gsub($avicus['maps-path'], 'https://maps.avicus.net/atlas')
    @path_remote = URI.escape(@path_remote)
    @doc = Nokogiri::XML(File.open(path))
  end

  # Get the average ratings for a map based on a version, or all versions.
  def ratings_average(version)
    if version == 'all'
      return MapRating.where(map_slug: slug).average('rating').to_s
    elsif version == 'current'
      return MapRating.where(map_slug: slug, map_version: self.version).average('rating').to_s
    else
      return MapRating.where(map_slug: slug, map_version: version).average('rating').to_s
    end
  end

  # Get all versions of this map which have been rated.
  def versions
    versions = {}
    versions['All Versions'] = 'all'
    versions[self.version + ' (Current Version)'] = 'current'
    MapRating.where(map_slug: self.slug).select(:map_version).map(&:map_version).uniq.sort_by { |ver| ver }.reverse!.each do |version|
      versions[version] = version.downcase.gsub('.', '_') unless version == self.version
    end
    versions
  end

  # Get all ratings which have feedback for a specific map version.
  def ratings_with_feedback(version)
    if version == 'all'
      return MapRating.where(map_slug: slug).where.not(feedback: nil).order('map_version DESC').all
    elsif version == 'current'
      return MapRating.where(map_slug: slug, map_version: self.version).where.not(feedback: nil).all
    else
      return MapRating.where(map_slug: slug, map_version: version).where.not(feedback: nil).all
    end
  end

  # Get the breakdown of ratings for the map.
  def ratings_breakdown(version = self.version)
    if version == 'all'
      ratings_query = MapRating.where(map_slug: slug).select(:rating)
    elsif version == 'current'
      ratings_query = MapRating.where(map_slug: slug, map_version: self.version).select(:rating)
    else
      ratings_query = MapRating.where(map_slug: slug, map_version: version).select(:rating)
    end

    ratings = {}
    5.times do |t|
      ratings[t+1] = ratings_query.where(rating: t+1).size
    end
    ratings
  end

  # Get the name of the map.
  def name
    @doc.css('map').attr('name').value
  end

  # Get the version of the map.
  # This should not be confused with the spec.
  def version
    @doc.css('map').attr('version').value
  end

  # Get the slug of the map.
  def slug
    slugAttr = @doc.css('map').attr('slug')
    return slugAttr.nil? ? name.gsub(/[\s]/, '-').mb_chars.normalize(:c).gsub(/[^\w-]/n, '').to_s.downcase : slugAttr.value
  end

  # Get the slug of the map with the version added to the end.
  def slug_version
    slug + '-' + version.gsub('.', '_')
  end

  # Get the path to the map.png from the map directory server.
  def png_path
    path = @path[0...@path.length-3] + 'png'
    path_remote = @path_remote[0...@path_remote.length-3] + 'png'
    File.exists?(path) ? path_remote : 'https://maps.avicus.net/atlas' + '/default_map.png'
  end

  # Get the path to the map_banner.png from the map directory server.
  def banner_path
    path = @path[0...@path.length-4] + '_banner.png'
    path_remote = @path_remote[0...@path_remote.length-4] + '_banner.png'
    File.exists?(path) ? path_remote : 'https://maps.avicus.net/atlas' + '/default_map_banner.png'
  end

  # Get an array of HTML formatted strings based on the authors of the map.
  def authors
    ret = {}
    @doc.css('map authors').children.each do |ch|
      uuid = ch.attr('uuid')
      role = ch.attr('role')
      next if uuid.blank?
      ret[uuid.gsub('-', '')] = ''
      if !role.blank?
        ret[uuid] = " <i>(#{role})</i>"
      end
    end
    ret
  end

  # Get the root category of the map based on it's location in the maps directory.
  def category
    path = @path_remote.split('/')
    return path[4]
  end

  # Get the objectives of the map based on elements found in the document.
  def objectives
    ret = ''
    monuments = @doc.css('map').xpath('//monument').size
    leakables = @doc.css('map objectives leakables').xpath('//leakable').size
    flags = @doc.css('map').xpath('//flag').size
    hills = @doc.css('map').xpath('//hill').size
    wools = @doc.css('map').xpath('//wool').size

    # Special
    lcs = !@doc.css('map').xpath('//last-competitor-standing').blank?
    lts = !@doc.css('map').xpath('//last-team-standing').blank?
    score = @doc.css('map').xpath('//scores')
    score_limit = score.first.attr('limit') unless score.blank?

    arr = []
    arr << "<strong>#{monuments}</strong> #{'Monument'.pluralize(monuments)}" if monuments > 0
    arr << "<strong>#{leakables}</strong> #{'Core'.pluralize(leakables)}" if leakables > 0
    arr << "<strong>#{flags}</strong> #{'Flag'.pluralize(flags)}" if flags > 0
    arr << "<strong>#{hills}</strong> #{'Hill'.pluralize(hills)}" if hills > 0
    arr << "<strong>#{wools}</strong> #{'Wool'.pluralize(wools)}" if wools > 0
    arr << 'Last Team Standing' if lts
    arr << 'Last Player Standing' if lcs
    arr << 'Highest Score' + (score_limit.blank? ? '' : " (Limit: #{score_limit})") if score
    return arr
  end

  # Check if the map is an FFA map.
  def ffa?
    !@doc.css('map ffa').blank?
  end

  # Get an HTML formatted string with colored team names based on the teams defined in XML.
  def teams
    return ['FFA (Free For All)'] if ffa?

    teams = []
    @doc.css('map teams').xpath('./team').each do |t|
      name = localized_sting(t.text)
      min = t.attr('min')
      max = t.attr('max')
      teams << "<h3 style='color: #{color_to_html(t.attr('color'))}'>#{name} <small style='color: black'>(#{min} - #{max} players)</small></h3>"
    end
    teams
  end
end

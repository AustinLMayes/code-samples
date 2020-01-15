## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../lib/worker"
require_relative "../lib/kubernetes"
require_relative "../lib/cloudflare"

class DnsUpdater < Worker
  include Kubernetes
  include Cloudflare

  @sizes = []

  def initialize()
    super("DNSUpater", 1.minute)
    @mapping = [
      {
        domain: "uhc",
        matchers: %w(uhc)
      },
      {
        domain: "pre",
        matchers: %w(dev)
      },
      {
        domain: "dev",
        matchers: %w(mapdev)
      }
    ]
  end

  def domain_for(pool)
    @mapping.select {|map| map[:matchers].include?(pool)}.map {|map| map[:domain]}[0]
  end

  def is_mapped?(domain)
    @mapping.any? {|map| map[:domain].to_s.downcase == domain.to_s.downcase}
  end

  def execute(itteration)
    expected = {}
    actual = {}
    nodes.each do |node|
      begin
        external = node.status.addresses.detect {|add| add.type == "ExternalIP"}.address
        pool = node.labels[:'doks.digitalocean.com/node-pool']
        domain = domain_for(pool)
        if domain.present?
          expected[domain] = [] unless expected[domain].present?
          expected[domain] << external
        end
      rescue Exception => e
        log("Failed to get DNS information for #{node.name}! #{e}", :critical)
        puts e.backtrace
      end
    end

    perform_on_zone do |zone|
      begin
        zone.dns_records.each do |r|
          prefix = r.name.split(".")[0]
          if is_mapped?(prefix)
            actual[prefix] = [] unless actual[prefix].present?
            actual[prefix] << r.content
          end
        end

        to_add = {}
        to_remove = {}
        expected.each do |domain, ips|
          if actual[domain].nil? || actual[domain].empty?
            to_add[domain] = ips
          else
            to_add[domain] = expected[domain] - actual[domain]
            to_remove[domain] = actual[domain] - expected[domain]
          end
        end

        if expected.empty?
          to_remove = actual
        end

        to_add.each do |domain, ips|
          ips.each do |ip|
            zone.dns_records.create("A", domain, ip)
            log "Adding DNS entry for #{domain}.walrus.network => #{ip}"
          end
        end
        to_remove.each do |domain, ips|
          zone.dns_records.select {|r| r.name.split(".")[0] == domain && r.type == "A" && ips.include?(r.content)}.each do |r|
            r.delete
            log "Removing DNS entry for #{r.name} (#{r.content})"
          end
        end
      rescue Exception => e
        log("Failed to update DNS information! #{e}", :critical)
        puts e.backtrace
      end
    end
  end
end

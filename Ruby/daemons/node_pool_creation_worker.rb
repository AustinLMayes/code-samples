## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../lib/worker"
require_relative "../lib/digital_ocean"

require 'securerandom'

class NodePoolCreationWorker < Worker
  include DigitalOcean
  include Kubernetes

  @@requests = {}
  @@watch_retry = Queue.new

  def self.need_node(id, pod_size, tags, pod_count=1)
    @@requests[:"#{id}"] = [] if @@requests[:"#{id}"].nil?
    data = @@requests[:"#{id}"]
    other = data.detect {|d| d[:memory] == pod_size}
    if other.present?
      other[:tags] = [other[:tags], tags].flatten.uniq
      other[:count] += pod_count
    else
      data << {
        tags: tags,
        count: pod_count,
        memory: pod_size
      }
    end
  end

  def initialize
    super("NodePoolCreator", 30.seconds)
  end

  def prepare
    collect_sizes
    register_watcher
    Thread.new do
      while command = @@watch_retry.pop
        command.call
      end
    end
  end

  def register_watcher
    watching = ""
    cluster.get_namespaces.each do |namespace|
      next if namespace.metadata.name.starts_with?("kube") # Ignore system namespaces
      watching += namespace.metadata.name + ", "
      watch(namespace)
    end
    log "Watching for automated pod requests on #{watching[0..-3]}"
  end

  def watch(namespace, count=1)
    Thread.new do
      begin
        watcher = cluster.watch_pods(label_selector: "auto/create=true", namespace: namespace.metadata.name)
        watcher.each do |notice|
          # Only care about added
          next unless notice.type == "ADDED"
          # Sanity namespace check
          next unless notice.object.metadata.namespace == namespace.metadata.name
          # Too old
          next if DateTime.parse(notice.object.metadata.creationTimestamp) < (Time.now - 2.minutes).utc
          sleep 2 # Give time to try and schedule
          pod = Pod.new(notice.object.metadata.name, notice.object.metadata.namespace)
          next if pod.labels[:'auto/handled'] == "true"
          next unless pod.status.phase == "Pending"
          scheduled = false
          pod.status.conditions.detect{|con| con.type == "PodScheduled"}.tap{|con| scheduled = !con.nil? && con.status.downcase == "true"}
          next if scheduled
          destination = pod.labels[:'auto/destination_id'] || pod.name.split("-")[0]
          if pod.labels[:'auto/tags'].nil?
            log "[WARN] Unable to create node request for #{pod.name} since it has no tags! Labels: #{pod.labels}"
            next
          end
          tags = pod.labels[:'auto/tags'].split(".S.") # Kube doesn't allow commas :(
          log "Queing node request for #{pod.name}"
          NodePoolCreationWorker.need_node(destination, pod.total_size, tags)
          pod.label({"auto/handled": "true"})
        end
        # The connection will close after a while. This is a k8s watcher issue so
        # we don't count it as an error on our end. Instead, we just start watching
        # again like nothing happened and hope for the best.
        @@watch_retry << proc { watch(namespace, count) }
      rescue Exception => e
        log("Exception while watching #{namespace.metadata.name}: #{e}", :critical)
        puts e.backtrace
        if count >= 5
          log("Giving up on #{namespace.metadata.name}", :critical)
        else
          @@watch_retry << proc { watch(namespace, count + 1) }
        end
      end
    end
  end

  def execute(itteration)
    return if @@requests.empty?
    to_process = @@requests
    @@requests = {}
    log "Handling #{to_process.size} requests..."
    cluster_id = digital_ocean.kubernetes_clusters.all.first.id
    to_process.each do |id, types|
      # All node requests assume that the pod has already tried to fit on a node and failed
      # Since this can sometimes mean that the pool exists but is full, we create a new
      # pool with the same tags instead of scaling up the current pool.
      # We do this so the pools can be individually deleted as pods go away instead of relying
      # on digitalocean's scaler which evicts pods
      size_count = pick_size_count(types)
      tags = []
      types.each do |t|
        tags << t[:tags]
      end
      tags.flatten!.uniq!
      log(
        "Creating #{id} " +
        "(#{size_count[1]} nodes @ #{size_count[0].slug}) " +
        "- #{(size_count[0].price_hourly * size_count[1]).round(2)} hourly"
      )
      node_pool = DropletKit::KubernetesNodePool.new(
        name: id.to_s + "-" + SecureRandom.urlsafe_base64(5).downcase.gsub("_", ""),
        size: size_count[0].slug,
        count: size_count[1],
        tags: tags
      )
      node_pool = digital_ocean.kubernetes_clusters.create_node_pool(node_pool, id: cluster_id)
    end
  end

  protected

  # Collect all usable sizes and sort them
  def collect_sizes
    region = digital_ocean.kubernetes_clusters.all.first.region
    @sizes = digital_ocean
    .sizes.all.select {
      |size| size.regions.include?(region) &&
      size.available &&
      size.slug.start_with?("s-", "c-", "g-")
    }
    @sizes.sort! {|a, b|
      [a.memory, a.vcpus, a.price_hourly] <=>
      [b.memory, b.vcpus, b.price_hourly]
    }
  end

  # Pick the pool and droplet size needed to run a collection of pods in the most
  #   cost effective manner. We itterate over each usuable droplet size and choose
  #   the size which splits the workload in the most even fassion and costs the
  #   least of the other choices.
  def pick_size_count(*pods)
    # Array of [size, count] for each pod type
    data = []
    # Total memory needed for all pods
    total_memory = 0
    # Largest single pod's memory needs
    largest_pod = 0
    # Total pods being used
    total_pods = 0
    pods.flatten.each do |pod|
      data << [pod[:memory], pod[:count]]
      total_memory += pod[:memory] * pod[:count]
      largest_pod = pod[:memory] if pod[:memory] > largest_pod
      total_pods += pod[:count]
    end
    largest_pod = largest_pod + 250 # 1/4gb padding for safety
    # Array of usable droplet sizes baased on min memory
    usable = @sizes
    .select {|s| s.memory >= largest_pod}
    # Just in case
    usable = @sizes if usable.empty?
    # Lowest usable size
    min_size = usable[0]
    return [min_size, 1] if total_pods == 1 # Can't go any lower and don't need any more space
    # Maximum prefered size that can handle all pods in one
    max_size = usable
    .select {|s| s.memory >= total_memory * total_pods * 1.13}[0] || usable[-1]
    # Range of sizes to actually check
    size_range = usable
    .select {|s| s.memory >= min_size.memory && s.memory <= max_size.memory * 1.5}
    # Array of [price, [size, node count]] for each size
    per_hour = [[min_size.price_hourly * total_pods, [min_size, 1]]]
    puts "Using #{usable.size} sizes"
    # Loop through each size and generate price estimations
    size_range.reverse.each do |size|
      catch :cant_fit do # Give up on this size if it can't support a single pod of any type
        nodes = 1 # Total nodes needed to support all pods
        total_percent = 0 # Percent of memory used on current node
        data.each do |set|
          percent = percent_of_size(set[0], size) # Percent of memory used by a single pod
          count = set[1] # Total pods of type
          throw :cant_fit if percent > 1 # Can't even fit one of pod
          # Try to fit each pod and add nodes when one becomes full
          while count > 1 do
            if total_percent + percent >= 1 # Node cap
              # Node full, add a node and reset percent
              nodes += 1 #
              total_percent = percent
            else
              # Not full yet
              total_percent += percent
            end
            count -= 1
          end
        end
        per_hour << [size.price_hourly * nodes, [size, nodes]]
      end
    end
    # Debug
    per_hour.each do |price|
      puts "#{price[0]} using #{price[1][1]} nodes of #{price[1][0].slug}"
    end
    # Lowest configuration
    lowest = per_hour.sort {|a, b| a[0] <=> b[0]}[0]
    # Debugging
    cost = lowest[0]
    res = lowest[1]
    puts "#{res[1]} nodes required"
    puts "pool has #{res[0].memory * res[1]}mb and pods require #{total_memory}mb"
    puts "#{res[0].price_hourly * res[1]} per hour"
    return [res[0], res[1]]
  end

  # Determine the percent that a slice of memory will take up of a size
  def percent_of_size(memory, size)
    ((memory * 1.13) / size.memory.to_f).to_f
  end
end

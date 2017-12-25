class Packs
  # Icons
  BASIC = {
      'Wrench': 'fa-wrench',
      'User X': 'fa-user-times',
      'TV': 'fa-tv',
      'Sliders': 'fa-sliders',
      'Spoon': 'fa-spoon'
  }

  HANDS = {
      'Grab': 'fa-hand-grab-o',
      'Lizard': 'fa-hand-lizard-o',
      'Point Down': 'fa-hand-o-down',
      'Point Left': 'fa-hand-o-left',
      'Point Right': 'fa-hand-o-right',
      'Point Up': 'fa-hand-o-up',
      'Paper': 'fa-hand-paper-o',
      'Peace': 'fa-hand-peace-o',
      'Pointer': 'fa-hand-pointer-o',
      'Rock': 'fa-hand-rock-o',
      'Scissors': 'fa-hand-scissors-o',
      'Spock': 'fa-hand-spock-o',
      'Stop': 'fa-hand-stop-o',
      'Thumbs Down': 'fa-thumbs-down',
      'Thumbs Up': 'fa-thumbs-up'
  }

  MONEY = {
      'BitCoin': 'fa-bitcoin ',
      'BTC': 'fa-btc',
      'Dollar': 'fa-dollar',
      'Euro': 'fa-euro ',
      'GG': 'fa-gg',
      'GG (Circle)': 'fa-gg-circle',
      'Money': 'fa-money',
      'USD': 'fa-usd',
      'Yen': 'fa-yen '
  }

  MEDICAL = {
      'Ambulance': 'fa-ambulance',
      'Heart': 'fa-heart',
      'Heart Beat': 'fa-heartbeat',
      'Plus': 'fa-plus-square',
      'MD': 'fa-user-md',
      'Wheelchair': 'fa-wheelchair'
  }

  BATTERY = {
      'Battery Empty': 'fa-battery-empty',
      'Battery 1': 'fa-battery-1',
      'Battery 2': 'fa-battery-2',
      'Battery 3': 'fa-battery-3',
      'Battery Full': 'fa-battery-full'
  }

  TRANSPORT = {
      'Bike': 'fa-bicycle',
      'Bus': 'fa-bus',
      'Cab': 'fa-cab',
      'Car': 'fa-car',
      'Jet': 'fa-fighter-jet',
      'Motorcycle': 'fa-motorcycle',
      'Plane': 'fa-plane',
      'Rocket': 'fa-rocket',
      'Ship': 'fa-ship',
      'Shuttle': 'fa-space-shuttle',
      'Subway': 'fa-subway',
      'Taxi': 'fa-taxi',
      'Train': 'fa-train',
      'Truck': 'fa-truck'
  }

  CHRISTMAS = {
      'Tree': 'fa-tree',
      'Snowflake': 'fa-snowflake-o',
      'Gift': 'fa-gift'
  }

  ICON_BASIC_PACK = Pack.new('Icons-Basic', BASIC, :icon_packs, :basic)
  ICON_HANDS_PACK = Pack.new('Icons-Hands', HANDS, :icon_packs, :hands)
  ICON_MONEY_PACK = Pack.new('Icons-Money', MONEY, :icon_packs, :money)
  ICON_MEDICAL_PACK = Pack.new('Icons-Medical', MEDICAL, :icon_packs, :medical)
  ICON_BATTERY_PACK = Pack.new('Icons-Battery', BATTERY, :icon_packs, :battery)
  ICON_TRANSPORT_PACK = Pack.new('Icons-Transport', TRANSPORT, :icon_packs, :transport)
  ICON_CHRISTMAS_PACK = Pack.new('Icons-Christmas', CHRISTMAS, :icon_packs, :christmas)

  ICON_PACKS = [ICON_BASIC_PACK,
                ICON_HANDS_PACK,
                ICON_MONEY_PACK,
                ICON_MEDICAL_PACK,
                ICON_BATTERY_PACK,
                ICON_TRANSPORT_PACK,
                ICON_CHRISTMAS_PACK]

  # Colors
  COLORS = %w(AliceBlue Aqua Aquamarine Blue BlueViolet Brown CadetBlue Coral CornflowerBlue Cyan DarkBlue DarkCyan DarkGoldenRod DarkGray DarkGrey DarkGreen DarkMagenta DarkOliveGreen DarkOrange DarkRed DarkSeaGreen DarkSlateBlue DarkSlateGray DarkSlateGrey DarkTurquoise DarkViolet DeepPink DeepSkyBlue DimGray DimGrey DodgerBlue ForestGreen Gold GoldenRod Gray Grey Green GreenYellow HotPink IndianRed Lavender LavenderBlush LawnGreen LightBlue LightCoral LightCyan LightGoldenRodYellow LightGray LightGrey LightGreen LightPink LightSeaGreen LightSkyBlue LightSlateGray LightSlateGrey LightSteelBlue LightYellow Lime LimeGreen Magenta MediumAquaMarine MediumBlue MediumPurple MediumSeaGreen MediumSlateBlue MediumSpringGreen MediumTurquoise MediumVioletRed MidnightBlue MintCream Navy Olive OliveDrab Orange OrangeRed PaleGoldenRod PaleGreen PaleTurquoise PaleVioletRed Pink PowderBlue Purple RebeccaPurple Red RosyBrown RoyalBlue SaddleBrown SandyBrown SeaGreen Silver SkyBlue SlateBlue SlateGray SlateGrey SpringGreen SteelBlue Tan Teal Turquoise Violet Yellow YellowGreen)
  BASIC = %w(Red Yellow Blue Green Black)
  BLUE = COLORS.select { |color| color.include?('Blue') || color.include?('Aqua') || color.include?('Cyan') || color.include?('Navy') || color.include?('Coral') || color.include?('Teal') || color.include?('Turquoise') }
  RED = COLORS.select { |color| color.include?('Red' || color.include?('Brick')) }
  GREEN = COLORS.select { |color| color.include?('Green') || color.include?('Lime') || color.include?('Mint') || color.include?('Olive') }
  GRAY = COLORS.select { |color| color.include?('Gray') || color.include?('Silver') || color.include?('Grey') }
  ORANGE = COLORS.select { |color| color.include?('Orange') || color.include?('Gold') }
  PINK_PURPLE = COLORS.select { |color| color.include?('Magenta') || color.include?('Pink') || color.include?('Purple') || color.include?('Lavender') || color.include?('Violet') }
  BROWN_TAN = COLORS.select { |color| color.include?('Brown') || color.include?('Tan') }
  YELLOW = COLORS.select { |color| color.include?('Yellow') }

  COLOR_BASIC_PACK = Pack.new('Colors-Basic', BASIC, :color_packs, :basic)
  COLOR_BLUE_PACK = Pack.new('Colors-Blue', BLUE, :color_packs, :blue)
  COLOR_RED_PACK = Pack.new('Colors-Red', RED, :color_packs, :red)
  COLOR_GREEN_PACK = Pack.new('Colors-Green', GREEN, :color_packs, :green)
  COLOR_ORANGE_PACK = Pack.new('Colors-Orange', ORANGE, :color_packs, :orange)
  COLOR_GRAY_PACK = Pack.new('Colors-Gray', GRAY, :color_packs, :gray)
  COLOR_PINK_PURPLE_PACK = Pack.new('Colors-Pink-Purple', PINK_PURPLE, :color_packs, :pink_purple)
  COLOR_BROWN_TAN_PACK = Pack.new('Colors-Brown-Tan', BROWN_TAN, :color_packs, :brown_tan)
  COLOR_YELLOW_PACK = Pack.new('Colors-Yellow', YELLOW, :color_packs, :yellow)

  COLOR_PACKS = [COLOR_BASIC_PACK,
                 COLOR_BLUE_PACK,
                 COLOR_RED_PACK,
                 COLOR_GREEN_PACK,
                 COLOR_ORANGE_PACK,
                 COLOR_GRAY_PACK,
                 COLOR_PINK_PURPLE_PACK,
                 COLOR_BROWN_TAN_PACK,
                 COLOR_YELLOW_PACK]
end
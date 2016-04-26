(ns search-commons.municipality-coords
  (:require [taoensso.timbre :as timbre]))

(def municipality-center-coordinates {20  [324985.061 6784290.193]
                                      5   [343283.693 6985908.347]
                                      9   [370399.239 7119863.552]
                                      10  [320419.463 6941690.657]
                                      16  [424107.961 6787177.678]
                                      18  [423973.595 6712625.921]
                                      19  [256466.432 6730562.694]
                                      35  [158822.854 6729493.063]
                                      43  [81955.243 6708737.644]
                                      46  [598558.852 6889526.62]
                                      47  [333183.937 7621155.059]
                                      49  [373141.793 6671842.958]
                                      50  [234244.614 6782805.543]
                                      51  [203687.001 6804427.289]
                                      52  [322278.842 7033389.229]
                                      60  [107852.269 6702648.75]
                                      61  [317782.563 6758126.877]
                                      62  [128235.803 6649720.648]
                                      65  [101907.517 6731881.553]
                                      69  [420009.881 7072752.717]
                                      71  [420259.467 7113039.091]
                                      72  [386132.743 7216008.887]
                                      74  [364436.612 7038493.345]
                                      75  [515941.166 6711453.585]
                                      76  [83259.953 6709868.001]
                                      77  [471688.588 6916548.869]
                                      78  [275351.763 6632121.57]
                                      79  [240430.755 6809897.841]
                                      81  [449540.7 6828294.135]
                                      82  [353443.388 6770728.292]
                                      84  [414296.1 7230313.387]
                                      86  [388956.52 6740238.317]
                                      111 [453463.893 6791815.576]
                                      90  [588860.141 6923679.811]
                                      91  [385290.516 6676095.708]
                                      97  [485447.83 6840112.633]
                                      98  [417678.811 6764666.918]
                                      99  [250074.425 6884790.014]
                                      102 [269816.977 6785031.334]
                                      103 [302320.619 6762086.122]
                                      105 [581071.1 7170667.465]
                                      106 [380676.664 6721220.436]
                                      283 [399964.812 6764445.563]
                                      108 [297481.426 6838190.343]
                                      109 [369183.727 6773539.485]
                                      139 [421272.133 7262215.898]
                                      140 [507697.692 7044405.471]
                                      142 [461004.04 6755833.244]
                                      143 [293748.734 6860936.583]
                                      145 [272864.837 6963321.06]
                                      146 [703401.082 6973628.974]
                                      153 [598869.045 6784964.454]
                                      148 [508621.714 7647343.63]
                                      149 [335579.517 6650637.005]
                                      151 [234209.841 6897772.649]
                                      152 [264019.195 6991392.777]
                                      164 [284902.877 6929823.253]
                                      165 [374408.503 6752895.941]
                                      167 [670772.996 6946754.329]
                                      169 [307614.296 6749828.43]
                                      170 [95923.634 6675546.284]
                                      171 [540998.188 6893668.076]
                                      172 [457304.172 6855407.618]
                                      174 [568230.915 6997007.107]
                                      176 [609764.55 7010578.471]
                                      177 [366806.503 6857160.579]
                                      178 [542629.283 6856687.11]
                                      179 [429457.53 6889450.04]
                                      181 [272258.211 6864059.999]
                                      182 [397996.487 6866718.14]
                                      186 [395595.884 6706432.064]
                                      202 [250354.817 6705926.867]
                                      204 [588347.608 6983508.775]
                                      205 [521148.218 7112151.718]
                                      208 [335891.361 7127808.371]
                                      211 [357034.203 6819289.813]
                                      213 [480247.955 6877915.826]
                                      214 [257559.685 6864898.306]
                                      216 [415856.744 6983191.083]
                                      217 [351357.706 7092248.47]
                                      218 [227716.116 6921505.875]
                                      223 [318929.903 6682469.467]
                                      224 [348584.987 6716618.143]
                                      226 [386780.881 6974806.169]
                                      230 [271626.504 6898780.529]
                                      231 [188859.795 6922445.756]
                                      232 [254533.8 6921071.649]
                                      233 [300226.944 7013665.052]
                                      235 [373312.157 6677500.527]
                                      236 [339789.866 7054777.771]
                                      239 [465600.691 7007646.221]
                                      240 [379803.293 7276182.602]
                                      320 [515924.731 7396557.869]
                                      241 [399004.477 7307840.903]
                                      322 [245300.135 6646185.645]
                                      244 [432477.724 7197907.337]
                                      245 [396342.609 6697357.648]
                                      246 [620146.463 6872824.123]
                                      248 [644543.173 6870003.723]
                                      249 [372268.923 6909132.209]
                                      250 [302058.017 6903017.711]
                                      254 [264813.609 6822571.771]
                                      255 [444225.215 7223290.005]
                                      256 [401332.052 7024255.945]
                                      257 [360400.431 6655266.257]
                                      260 [656914.563 6886345.869]
                                      261 [417898.595 7522902.577]
                                      263 [477064.473 7061559.043]
                                      265 [401936.907 7004174.317]
                                      271 [253628.638 6806921.26]
                                      272 [319002.862 7091754.137]
                                      273 [377096.754 7462057.568]
                                      275 [464672.099 6947629.297]
                                      276 [646610.893 6964974.707]
                                      280 [186629.981 6979003.692]
                                      284 [288332.78 6731356.852]
                                      285 [502018.014 6693780.529]
                                      286 [487715.689 6756026.132]
                                      287 [200264.735 6902221.303]
                                      288 [319238.243 7062142.533]
                                      290 [629914.441 7119794.508]
                                      291 [403597.655 6832284.296]
                                      295 [149438.137 6713375.043]
                                      297 [533333.539 6967991.62]
                                      300 [322027.51 6966325.106]
                                      301 [250951.55 6955903.003]
                                      304 [175988.4 6739633.939]
                                      305 [603411.305 7318403.39]
                                      312 [376438.499 6994209.055]
                                      316 [405925.637 6748192.277]
                                      317 [444148.3 7093062.359]
                                      318 [157322.079 6638466.574]
                                      319 [251162.209 6786052.318]
                                      398 [428735.971 6761057.472]
                                      399 [247291.088 6982217.053]
                                      400 [216612.296 6762322.471]
                                      407 [456165.378 6722632.554]
                                      402 [531466.946 7027246.398]
                                      403 [328816.882 7011298.964]
                                      405 [568188.827 6761226.33]
                                      408 [304688.842 6985333.373]
                                      410 [448623.174 6918165.846]
                                      413 [260604.047 6838628.621]
                                      416 [543010.72 6771308.629]
                                      417 [106670.895 6658929.278]
                                      418 [327668.208 6805527.495]
                                      420 [547713.879 6932200.81]
                                      421 [385395.14 7043325.471]
                                      422 [659631.121 7026888.728]
                                      423 [252950.813 6719509.475]
                                      425 [435809.936 7172683.233]
                                      426 [622573.843 6936821.483]
                                      444 [333983.153 6686171.102]
                                      430 [281094.093 6758140.359]
                                      433 [354715.146 6731325.489]
                                      434 [458156.79 6692428.017]
                                      435 [431199.484 6850491.981]
                                      436 [411210.271 7193752.931]
                                      438 [124793.887 6683611.837]
                                      440 [280416.227 7087905.237]
                                      441 [528638.082 6756187.609]
                                      442 [194022.235 6821666.606]
                                      445 [202515.839 6661189.565]
                                      475 [192420.584 7001195.289]
                                      476 [511188.874 7002337.96]
                                      478 [107894.564 6682168.981]
                                      480 [277455.088 6725249.955]
                                      481 [226310.817 6724004.076]
                                      483 [379422.968 7133175.124]
                                      484 [200984.474 6870762.824]
                                      489 [536122.478 6730664.473]
                                      491 [514801.595 6852187.087]
                                      494 [458857.498 7181768.762]
                                      495 [389810.412 6929177.602]
                                      498 [365601.419 7530069.603]
                                      499 [211961.392 7032208.638]
                                      500 [427983.719 6890536.794]
                                      503 [228949.645 6744103.558]
                                      504 [436670.766 6723475.882]
                                      505 [405939.674 6726250.738]
                                      508 [364429.313 6886126.399]
                                      507 [493387.641 6801479.717]
                                      529 [215259.747 6707452.295]
                                      531 [230066.659 6813250.332]
                                      532 [442758.819 6762667.216]
                                      534 [554552.233 7010650.458]
                                      535 [403140.928 7090266.57]
                                      536 [306505.546 6818582.821]
                                      538 [235812.069 6733970.036]
                                      540 [333240.962 6707707.823]
                                      541 [611164.112 7055241.531]
                                      543 [376608.913 6704946.69]
                                      545 [196753.477 6945123.581]
                                      560 [435862.324 6741127.497]
                                      561 [265081.148 6757313.741]
                                      562 [366040.147 6840303.568]
                                      563 [397075.214 7127192.69]
                                      564 [458020.566 7212721.004]
                                      567 [418304.12 7205625.537]
                                      309 [599871.831 6956108.644]
                                      576 [403768.109 6805122.983]
                                      577 [264781.998 6709544.543]
                                      578 [534982.195 7144481.56]
                                      580 [639478.303 6836717.751]
                                      581 [289832.756 6886230.944]
                                      599 [300697.771 7052036.817]
                                      583 [520268.592 7447341.919]
                                      854 [381298.255 7409156.406]
                                      584 [372507.486 7016684.237]
                                      588 [469991.35 6818553.543]
                                      592 [407660.335 6907776.449]
                                      593 [510514.014 6906718.897]
                                      595 [484110.359 7017574.13]
                                      598 [272568.34 7073744.732]
                                      601 [429328.028 7032037.863]
                                      604 [319050.215 6815817.869]
                                      607 [619858.601 6975941.212]
                                      608 [238373.228 6851239.278]
                                      609 [210579.917 6841296.238]
                                      611 [409839.019 6706251.166]
                                      638 [429450.221 6672734.386]
                                      614 [547145.463 7337108.839]
                                      615 [505971.527 7258385.267]
                                      616 [422516.579 6724932.51]
                                      618 [621849.204 6844113.068]
                                      619 [291430.123 6781874.149]
                                      620 [535224.987 7192576.549]
                                      623 [563502.465 6820266.135]
                                      624 [482514.252 6696854.34]
                                      625 [356738.403 7152694.21]
                                      626 [446536.973 7059582.762]
                                      630 [474329.529 7104967.176]
                                      631 [193557.315 6779372.789]
                                      635 [367651.599 6804396.307]
                                      636 [257743.61 6747353.41]
                                      678 [372553.579 7173072.833]
                                      710 [301732.848 6649806.93]
                                      680 [233415.44 6715908.242]
                                      681 [567363.222 6881469.34]
                                      683 [482569.327 7317769.866]
                                      684 [197510.692 6791644.225]
                                      686 [485453.424 6945797.737]
                                      687 [569948.966 7041027.758]
                                      689 [618269.984 6806595.74]
                                      691 [399807.186 7056567.084]
                                      694 [376677.001 6732787.791]
                                      696 [519066.577 6816234.868]
                                      697 [568727.225 7151819.557]
                                      698 [448871.188 7391310.797]
                                      700 [593848.645 6806270.685]
                                      702 [345520.897 6874297.698]
                                      704 [242238.551 6728027.198]
                                      707 [638657.082 6910033.932]
                                      729 [404419.632 6953818.074]
                                      732 [582856.917 7431698.785]
                                      734 [292471.12 6695002.453]
                                      736 [121668.056 6729319.126]
                                      790 [282177.323 6815290.932]
                                      738 [260531.877 6692645.893]
                                      739 [530322.7 6783419.938]
                                      740 [600248.494 6872001.979]
                                      742 [564813.008 7502677.828]
                                      743 [290903.32 6963395.506]
                                      746 [381627.514 7081586.21]
                                      747 [231591.059 6869996.165]
                                      748 [395635.271 7181615.326]
                                      791 [450472.518 7133946.942]
                                      749 [533156.486 6994838.282]
                                      751 [415198.799 7292308.738]
                                      753 [406062.825 6690712.14]
                                      755 [345801.169 6673474.004]
                                      758 [481800.0 7478416.0]
                                      759 [359274.233 6970848.718]
                                      761 [311508.033 6724784.308]
                                      762 [532783.865 7067940.255]
                                      765 [570602.193 7103120.753]
                                      766 [152871.701 6677940.616]
                                      768 [575263.781 6846667.357]
                                      771 [122110.798 6696729.902]
                                      775 [519744.643 6799903.537]
                                      777 [599959.957 7215051.931]
                                      778 [507977.968 6943221.841]
                                      781 [431965.547 6818954.037]
                                      783 [250553.281 6771990.407]
                                      831 [558622.442 6789202.979]
                                      832 [562471.36 7271530.01]
                                      833 [203294.149 6725898.265]
                                      834 [327566.206 6743079.745]
                                      837 [334704.582 6836809.175]
                                      838 [266124.404 6724248.897]
                                      844 [490667.936 6982431.718]
                                      845 [413713.417 7335927.175]
                                      846 [232400.249 6941338.558]
                                      848 [675615.039 6907193.349]
                                      849 [366406.174 7070267.361]
                                      850 [451706.922 6886751.457]
                                      851 [376855.473 7325306.086]
                                      853 [239745.77 6713200.763]
                                      857 [575517.414 6961630.093]
                                      858 [390090.364 6703401.922]
                                      859 [441292.01 7178322.088]
                                      863 [338794.804 6949330.025]
                                      886 [239891.842 6826198.112]
                                      887 [311014.727 6776925.632]
                                      889 [491524.991 7193031.762]
                                      890 [498117.374 7727832.798]
                                      892 [418077.835 6927774.506]
                                      893 [266631.276 7052369.149]
                                      895 [179921.548 6764132.529]
                                      785 [494970.398 7154337.606]
                                      905 [221251.467 7007398.113]
                                      908 [341903.618 6790608.729]
                                      911 [586764.148 7068931.415]
                                      92  [386790.153 6688062.918]
                                      915 [564583.142 6905894.534]
                                      918 [209726.799 6738219.706]
                                      921 [468768.453 6974511.657]
                                      922 [311695.63 6799525.515]
                                      924 [345583.58 7033791.074]
                                      925 [499943.972 7079585.048]
                                      926 [407589.599 7146634.589]
                                      927 [356610.307 6699723.311]
                                      931 [440531.421 6997478.521]
                                      934 [346063.379 7009456.957]
                                      935 [533334.638 6710380.827]
                                      936 [328917.823 6908894.433]
                                      941 [136748.192 6702265.555]
                                      942 [251168.272 7001591.411]
                                      946 [253725.829 7031958.334]
                                      972 [450947.313 7253324.734]
                                      976 [383415.471 7372475.269]
                                      977 [385101.228 7106050.448]
                                      980 [319149.048 6861125.286]
                                      981 [296962.623 6746764.927]
                                      989 [356084.611 6941267.091]
                                      992 [439969.956 6953181.977]})

(defn- default-coords [org-id]
  ;; Default to Helsinki
  (timbre/warn "Could not find map center coordinates for municipality id" org-id)
  [385290.516 6676095.708])

(defn center-coordinates [municipality-id]
  (or (get municipality-center-coordinates municipality-id) (default-coords municipality-id)))

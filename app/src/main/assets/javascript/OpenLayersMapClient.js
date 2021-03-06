if (!Array.from) {
  Array.from = (function () {
    var toStr = Object.prototype.toString;
    var isCallable = function (fn) {
      return typeof fn === 'function' || toStr.call(fn) === '[object Function]';
    };
    var toInteger = function (value) {
      var number = Number(value);
      if (isNaN(number)) { return 0; }
      if (number === 0 || !isFinite(number)) { return number; }
      return (number > 0 ? 1 : -1) * Math.floor(Math.abs(number));
    };
    var maxSafeInteger = Math.pow(2, 53) - 1;
    var toLength = function (value) {
      var len = toInteger(value);
      return Math.min(Math.max(len, 0), maxSafeInteger);
    };

    // The length property of the from method is 1.
    return function from(arrayLike/*, mapFn, thisArg */) {
      // 1. Let C be the this value.
      var C = this;

      // 2. Let items be ToObject(arrayLike).
      var items = Object(arrayLike);

      // 3. ReturnIfAbrupt(items).
      if (arrayLike == null) {
        throw new TypeError('Array.from requires an array-like object - not null or undefined');
      }

      // 4. If mapfn is undefined, then let mapping be false.
      var mapFn = arguments.length > 1 ? arguments[1] : void undefined;
      var T;
      if (typeof mapFn !== 'undefined') {
        // 5. else
        // 5. a If IsCallable(mapfn) is false, throw a TypeError exception.
        if (!isCallable(mapFn)) {
          throw new TypeError('Array.from: when provided, the second argument must be a function');
        }

        // 5. b. If thisArg was supplied, let T be thisArg; else let T be undefined.
        if (arguments.length > 2) {
          T = arguments[2];
        }
      }

      // 10. Let lenValue be Get(items, "length").
      // 11. Let len be ToLength(lenValue).
      var len = toLength(items.length);

      // 13. If IsConstructor(C) is true, then
      // 13. a. Let A be the result of calling the [[Construct]] internal method
      // of C with an argument list containing the single item len.
      // 14. a. Else, Let A be ArrayCreate(len).
      var A = isCallable(C) ? Object(new C(len)) : new Array(len);

      // 16. Let k be 0.
      var k = 0;
      // 17. Repeat, while k < len… (also steps a - h)
      var kValue;
      while (k < len) {
        kValue = items[k];
        if (mapFn) {
          A[k] = typeof T === 'undefined' ? mapFn(kValue, k) : mapFn.call(T, kValue, k);
        } else {
          A[k] = kValue;
        }
        k += 1;
      }
      // 18. Let putStatus be Put(A, "length", len, true).
      A.length = len;
      // 20. Return A.
      return A;
    };
  }());
}

var applicationType = Backend.Type.ANDROID;

var map;
var app = {};
var aisSearchModule = new VesselAisSearchModule();
var barentswatchLayersTranslator = new BarentswatchLayersTranslator("nb_NO");
var statensKartverkCommunicator = new StatensKartverkCommunicator();
var barentswatchCommunicator = new BarentswatchMapServicesCommunicator();
var tileLayerWMTS = statensKartverkCommunicator.CreateTileLayerWTMSFromSource(statensKartverkCommunicator.CreateSourceWmts("sjokartraster"), "base", "Norges grunnkart");
/*var openSeaMapLayer = new ol.layer.Tile({
    source: new ol.source.OSM({
        attributions: [
            'All maps © <a href="http://www.openseamap.org/">OpenSeaMap</a>',
            ol.source.OSM.ATTRIBUTION
        ],
        opaque: false,
        url: 'https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png'
    })
});*/
//var polar = statensKartverkCommunicator.CreateTileLayerWTMSFromSource(statensKartverkCommunicator.CreateSourceWmts("sirkumpolar_grunnkart"), "base", "Norges grunnkart");
var barentswatchObjectFactory = new BarentswatchApiObjectFactory();
var backendCommunicator = BackendFactory.createBackend(applicationType);

var container = document.getElementById('popup');
var content = document.getElementById('popup-content');
var closer = document.getElementById('popup-closer');

// __GEOLOCATION
var geolocator = null;
var sensor = false;
// __END_GEOLOCATION

// __BEGIN_CONTROLS_AND_INTERACTIONS_
var defaultInteractions = ol.interaction.defaults({altShiftDragRotate: false, pinchRotate: false});

function debounce(func, wait, immediate) {
    var timeout;
    return function () {
        var context = this, args = arguments;
        var later = function () {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        var callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };
};


map = new ol.Map({
    //renderer: (['webgl', 'canvas']),
    layers: [tileLayerWMTS],
    target: 'map',
    interactions: defaultInteractions,
    controls: ol.control.defaults({
        attribution: false,
    }),
    view: new ol.View({
        center: ol.proj.transform([15.5, 68], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6,
        minZoom: 3,
        maxZoom: 15
    })
});

app.DebounceSelect = function () {
    this.selectInteraction = new ol.interaction.Select({
        condition: ol.events.condition.singleclick,
    });

    var handleEventDebounce = debounce(function (evt) {
        return ol.interaction.Select.handleEvent.call(this.selectInteraction, evt);
    }.bind(this), 100);

    ol.interaction.Interaction.call(this, {
        handleEvent: function (evt) {
            handleEventDebounce(evt);
            // always return true so that other interactions can
            // also process the event
            return true;
        }
    });
};
ol.inherits(app.DebounceSelect, ol.interaction.Interaction);

app.DebounceSelect.prototype.setMap = function (map) {
    this.selectInteraction.setMap(map);
};
var debounceSelect = new app.DebounceSelect();
map.addInteraction(debounceSelect);

//var sidebar = new ol.control.Sidebar({element: 'sidebar', position: 'left'});
//map.addControl(sidebar);

// Set extent
//TODO: SET MORE ACCURAT MAP EXTENT, IGNORE ANTARTICA ETC, only show greenland, norway, russia and england. ALlow some towards canada

//WAVE WMS TEST
map.addLayer(barentswatchCommunicator.createWaveWarningSingleTileWMS());
map.addLayer(barentswatchCommunicator.createIceEdgeSingleTileWMS());
setVsibilityOfLayerByName("bwdev:iceedge_latest", false); //TODO: REMOVE ME
populateMap();
var popupOverlay = new ol.Overlay({
    element: container,
    autoPan: true,
    autoPanAnimation: {
        duration: 250
    }
});

function buggyZoomToMyPosition() {
    geolocator = new Geolocator(true, tileLayerWMTS.getSource().getProjection());
    var localGeolocationObject = geolocator.getGeolocation();
    localGeolocationObject.on('change', function () {
        map.setView(new ol.View({
            center: localGeolocationObject.getPosition(),
            zoom: 13
        }));
    });
}

function dispatchDataToBottomsheet(feature, type) {
    console.time("dispatchData");
    console.time("factory");
    var _feature = barentswatchObjectFactory.create(type);
    console.timeEnd("factory");
    console.time("parse");
    _feature.parseObject(feature);
    console.timeEnd("parse");
    //DISPATCH HERE
    console.time("showBottomsheet");
    backendCommunicator.showBottmsheet(_feature);
    console.timeEnd("showBottomsheet");
    console.timeEnd("dispatchData");
}

// TODO: REFACTOR ME
var displayFeatureInfo = function (pixel) {
    var features = [];
    var layers = [];
    console.time("DF");
    console.time("DF forEach");

    map.forEachFeatureAtPixel(pixel, function (feature, layer) {
        features.push(feature);
        layers.push(layer);
    }, {
        hitTolerance: 10
    });
    console.timeEnd("DF forEach");
    if (!Array.isArray(layers) || !layers.length) {
        // No features, escape early
        console.log("No features return");
        console.timeEnd("DF");
        return;
    }

    //Handle only last selected feature
    var selectedLayerName = layers[layers.length - 1].get("title");
    switch (selectedLayerName) {
        case "icechart":
            //dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.ICE_CONSENTRATION);
            break;
        case "npdsurveyongoing":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.ONGOING_SEISMIC);
            break;
        case "npdsurveyplanned":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.PLANNED_SEISMIC);
            break;
        case "npdfacility":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.SEABOTTOM_INSTALLATION);
            break;
        case "jmelding":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.JMESSAGE);
            break;
        case "coastalcodregulations":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.COASTLINES_COD);
            break;
        case "coralreef":
            dispatchDataToBottomsheet(features[features.length - 1], BarentswatchApiObjectTypes.CORAL_REEF);
            break;
        case "AIS":
            if (features[features.length - 1].N.features.length > 1) {
                console.log("AIS return");
                console.timeEnd("DF");
                return;
            }
            dispatchDataToBottomsheet(features[features.length - 1].N.features[0], BarentswatchApiObjectTypes.AIS);
            break;
        case "Tools":
            if (features[features.length - 1].N.features.length > 1) {
                console.log("Tools return");
                console.timeEnd("DF");
                return;
            }
            dispatchDataToBottomsheet(features[features.length - 1].N.features[0], BarentswatchApiObjectTypes.TOOL);
            break;
        case "Tools-nets":
        case "Tools-crabpot":
        case "Tools-mooring":
        case "Tools-longLine":
        case "Tools-danishPurseSeine":
        case "Tools-sensorcables":
        case "Tools-unknown":
            if (features[0].superHack !== undefined && features[0].superHack) {
                for (var i = 0; i < features.length; i++) {
                    features[i].superHack = false;
                }
                dispatchDataToBottomsheet(features[features.length - 1].N.features[0], BarentswatchApiObjectTypes.TOOL);
            }
            if (features[features.length - 1].N.features.length > 1) {
               console.log("Tools unknown return");
               console.timeEnd("DF");
               return;
            }
            dispatchDataToBottomsheet(features[features.length - 1].N.features[0], BarentswatchApiObjectTypes.TOOL);
            break;
        default:
            popupOverlay.setPosition(undefined);
            closer.blur();
            break;
    }
    console.timeEnd("DF");
};

function locateTool(toolOwner, toolId) {
    var owner = aisSearchModule.getVessel(toolOwner);
    var tool = null;
    for (var i in owner.tools) {
        if (owner.tools[i].get("id") === +toolId) {
            tool = owner.tools[i];
            break;
        }
    }
    if (tool !== null) {
        var interactionSelection = BarentswatchStylesRepository.BarentswatchToolSelectionStyle();
        map.getView().fit(tool.getGeometry(), map.getSize());
        interactionSelection.getFeatures().push(tool);
        interactionSelection.dispatchEvent({
            type: 'select',
            selected: [tool],
            deselected: []
        });
    }
}

function showVesselAndBottomsheet(vesselName) {
    var vessel = aisSearchModule.getVessel(vesselName);
    if (vessel != null) {
        locateVessel(vesselName);
        dispatchDataToBottomsheet(vessel, BarentswatchApiObjectTypes.AIS);
    }
}


function locateVessel(vesselName) {
    console.time("LocateVessel");
    var interactionSelection = BarentswatchStylesRepository.BarentswatchAisSelectionStyle();
    console.time("Fit");
    map.getView().fit(aisSearchModule.getVessel(vesselName).getGeometry(), map.getSize());
    console.timeEnd("Fit");
    console.time("Push");
    interactionSelection.getFeatures().push(aisSearchModule.getVessel(vesselName));
    console.timeEnd("Push");
    console.time("DispatchEvent");
    interactionSelection.dispatchEvent({
        type: 'select',
        selected: [aisSearchModule.getVessel(vesselName)],
        deselected: []
    });
    console.timeEnd("DispatchEvent");
    console.timeEnd("LocateVessel");
}

function getAllMapLayers() {
    var mLayers = [];
    map.getLayers().forEach(function (layer) {
        //If this is actually a group, we need to create an inner loop to go through its individual layers
        if (layer instanceof ol.layer.Group) {
            layer.getLayers().forEach(function (groupLayer) {
                mLayers.push(groupLayer);
            });
        }
        else {
            mLayers.push(layer);
        }
    });
    return mLayers;
}

function getLayersByNameAndVisibilityState() {
    var retval = [];
    getAllMapLayers().forEach(function (layer) {
        retval.push({name: layer.get("title"), visibility: layer.getVisible()});
    });
    return retval;
}

function getLayersBySaneNameAndVisibilityState() {
    var retval = [];
    var toolFound = false;
    var mapLayers = getAllMapLayers();
    getAllMapLayers().forEach(function (layer) {
        if (toolFound === true && barentswatchLayersTranslator.translateFromLayerToSaneName(layer.get("title")) === "Redskaper") {
            return;
        }
        if (toolFound === false && barentswatchLayersTranslator.translateFromLayerToSaneName(layer.get("title")) === "Redskaper") {
            toolFound = true;
        }
        retval.push({
            name: barentswatchLayersTranslator.translateFromLayerToSaneName(layer.get("title")),
            visibility: layer.getVisible()
        });
    });
    return retval;
}

function setVsibilityOfLayerByPrettyName(name, visiblity) {
    var layers = getAllMapLayers();
    layers.forEach(function (layer) {
        if (barentswatchLayersTranslator.translateFromLayerToSaneName(layer.get("title")) === name) {
            layer.setVisible(visiblity);
        }
    });
}

function setVsibilityOfLayerByName(name, visiblity) {
    var layers = getAllMapLayers();
    layers.forEach(function (layer) {
        if (layer.get("title") === name) {
            layer.setVisible(visiblity);
        }
    });
}

function populateMap() {
    barentswatchCommunicator.setMap(map);
    barentswatchCommunicator.setAISSearchPlugin(aisSearchModule);
    //document.addEventListener('DOMContentLoaded', function () { // TODO: REPLACE THIS
//        var elems = document.querySelectorAll('.autocomplete');
//        var instances = M.Autocomplete.init(elems, options);
//    });
    var iceChartLayer = barentswatchCommunicator.createApiServiceVectorLayer("icechart", BarentswatchStylesRepository.BarentswatchIceChartStyle);
    var ongoingSeismic = barentswatchCommunicator.createApiServiceVectorLayer("npdsurveyongoing", BarentswatchStylesRepository.BarentswatchActiveSeismicStyle);
    var plannedSeismic = barentswatchCommunicator.createApiServiceVectorLayer("npdsurveyplanned", BarentswatchStylesRepository.BarentswatchPlannedSeismicStyle);
    var facilityLayer = barentswatchCommunicator.createApiServiceVectorLayer("npdfacility", BarentswatchStylesRepository.BarentswatchSeaBottomInstallationsStyle);
    var legalMessages = barentswatchCommunicator.createApiServiceVectorLayer("jmelding", BarentswatchStylesRepository.BarentswatchJMessagesStyle);
    var coastalcodRegulations = barentswatchCommunicator.createApiServiceVectorLayer("coastalcodregulations", BarentswatchStylesRepository.BarentswatchCoastalRegulationStyle);
    var coralReef = barentswatchCommunicator.createApiServiceVectorLayer("coralreef", BarentswatchStylesRepository.BarentswatchCoralReefStyle);

    //VISIBILITY THING TODO: REMOVE ME
    iceChartLayer.setVisible(false);
    ongoingSeismic.setVisible(false);
    plannedSeismic.setVisible(false);
    facilityLayer.setVisible(false);
    legalMessages.setVisible(false);
    coastalcodRegulations.setVisible(false);
    coralReef.setVisible(false);

    //__END_VISIBILITY THING TODO: REMOVE ME

    barentswatchCommunicator.createAisVectorLayer(backendCommunicator, null);
    barentswatchCommunicator.createToolsVectorLayer(backendCommunicator);

    map.addLayer(iceChartLayer);
    map.addLayer(ongoingSeismic);
    map.addLayer(plannedSeismic);
    map.addLayer(facilityLayer);
    map.addLayer(legalMessages);
    map.addLayer(coastalcodRegulations);
    map.addLayer(coralReef);

    // SELECT HANDLERS
    // __BEGIN_SELECTION_STYLES_
    map.addInteraction(BarentswatchStylesRepository.BarentswatchIceChartSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchActiveSeismicSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchPlannenSeismicSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchSeaBottomInstallationsSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchJMessagesSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchCoastalRegulationSelectionStyle());
    map.addInteraction(BarentswatchStylesRepository.BarentswatchCoralReefSelectionStyle());
    // __END_SELECTION_STYLES_

    // TEST GLOBAL SELECTOR
    map.on("singleclick", function (evt) {
        displayFeatureInfo(evt.pixel);
    });


    map.getView().on('change:resolution', function (evt) {
        var view = evt.target;

        this.getLayers().getArray().map(function (layer) {
            var source = layer.getSource();
            if (source instanceof ol.source.Cluster) {
                var distance = source.getDistance();
                if (view.getZoom() >= 15 && distance > 0) {
                    source.setDistance(0);
                }
                else if (view.getZoom() < 15 && distance == 0) {
                    source.setDistance(6);
                }
            }
        });
    }, map);
}

function corsErrBack(error) {
    console.log("Error occurred during a cors request: The following error was raised: " + error);
}

// __SIMPLE_GEOLOCATION_INTERFACE_
function populateUserPosition(callback) {
    /*Based on W3C standards specification: http://dev.w3.org/geo/api/spec-source.html */
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(callback, fail, {timeout: 60000});
        return true;
    } else {
        return false;
    }
}

function zoomToUserPosition() {
    sensor = populateUserPosition(function (position) {
        var userPosition = ol.proj.transform([position.coords.longitude, position.coords.latitude], 'EPSG:4326', 'EPSG:3857');
        map.setView(new ol.View({
            center: userPosition,
            zoom: 10
        }));
    });
}

function fail() {
    alert("Noe gikk galt, venligst sjekk om du har internett- eller Ggps (gps, glonass osv) forbindelse");
}

// __END_SIMPLE_GEOLOCATION_INTERFACE_

// __BEGIN_POPUP_
/**
 * Add a click handler to hide the popup.
 * @return {boolean} Don't follow the href.
 */
closer.onclick = function () {
    popupOverlay.setPosition(undefined);
    closer.blur();
    return false;
};

// __END_POPUP
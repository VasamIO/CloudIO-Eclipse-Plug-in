io.controller('AddCSVColumnController', function(C, $timeout, Store, CloudIOModel, Cache, Logger, Session, $rootScope, $scope,$state,$stateParams,toaster){
  //initialization block
  var dmConnectorStore = Store.get("IODMConnector","_iodmconnectoradd");
  var dmFetchObjStore = Store.get("IODMFetchObjects","_iodmfetchstore");
  var dmObjStore = Store.get("IODMConnObjDef","_iodmobjstore");
  var scheduler = Store.get("Scheduler","_scheduler1");
  var dmObjAttributesStore = Store.get("IODMConnObjDefDetails","_iodmattribstore");
  $scope.noObjects = true;
  $scope._isNew = ($stateParams.objid == 'new');

  $scope.types = [
    {VALUE:"VARCHAR2"},{VALUE:"CLOB"},
    {VALUE:"TIMESTAMP"},{VALUE:"DATE"},{VALUE:"NUMBER"}
  ];
  //--> end

  //main methods


  $scope.saveColumnMetaData = function() {
    $scope.datarow.objDefId = $stateParams.objid;
    dmObjAttributesStore.saveRow($scope.datarow).then(function(result){
      if(result.$error) {
        toaster.pop("error",result.errorMessage); 
      }
      $scope.back();
    });
  };



  $scope.setupObject = function() {
    $scope.noObjects = false;
  };

  if($stateParams.objid !== 'new') {
    var objId = $stateParams.objid;
    dmObjAttributesStore.request.data.objDefId = objId;
    dmObjAttributesStore.query().then(function(result){
      if(result.$error) {
        toaster.pop("error",result.errorMessage); 
      } else {
        $scope.objAttribs = result.data;
      }
    });
  }
  $scope.showStatusMeaning = function(src) {
    if("Y".equals(src)) {
      return "Yes";
    } 
    return "No";
  };
  //--> end


  // navigation methods
  $scope.back = function() {
    $state.go("app.addcsvobject",{"conid":$stateParams.conid,"objid":$stateParams.objid});
  };
  //--> end
});

angular.module('mooseheadModule')
    .controller('AddWorkshopCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.showNoAccess=false;
            $scope.needLogin = false;
            $scope.needAccess = false;
            $scope.workshopType = "NORMAL_WORKSHOP";
            $http({method: "GET", url: "data/userLogin"})
                .success(function(userobj) {
                    if (!(userobj && userobj.id)) {
                        $scope.showNoAccess = true;
                        $scope.needLogin = true;
                        return;
                    }
                    if (!userobj.admin) {
                        $scope.showNoAccess = true;
                        $scope.needAccess = true;
                        $scope.googleid = userobj.id;
                        return;

                    }
                });

            $scope.showMessage = false;

            $scope.addWorkshop = function() {
                $scope.showMessage = false;

                var postData = {
                    slug: $scope.slug,
                    title: $scope.title,
                    description: $scope.description,
                    startTime: $scope.startTime,
                    endTime: $scope.endTime,
                    openTime: $scope.openTime,
                    maxParticipants: $scope.maxParticipants,
                    workshopType: $scope.workshopType
                };

                $http({
                    method: "POST",
                    url: "data/addWorkshop",
                    data: postData
                }).success(function(data) {
                    if (data.status === "OK") {
                        $scope.message = "Workshop added";
                        $scope.showForm = false;
                    } else {
                        $scope.message = data.message;
                    }
                    $scope.showMessage = true;

                }).error(function(data, status, headers, config) {
                    $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
                    $scope.showMessage = true;
                });
            }
        }]);



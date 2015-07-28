angular.module('mooseheadModule')
    .controller('ShowMyReservationCtrl', ['$scope', '$http','$routeParams',
        function($scope, $http,$routeParams) {

            $scope.email = $routeParams.email;
            $scope.reservations = [];
            $scope.message="Please wait...";
            $scope.showMessage = true;

            var urlWithPara = "data/myReservations?email=" + encodeURIComponent($scope.email) + "&" + "xx=" + Date.now();

            $http({
                method: "GET",
                url: urlWithPara
            }).success(function (data) {
                if (data && data.length > 0) {
                    $scope.reservations = data;
                    $scope.showMessage = false;
                } else {
                    $scope.message = "No reservations found";
                }
            }).error(function (data, status, headers, config) {
                $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
            });

            $scope.computeText = function(reservation) {
                var text = reservation.workshopname;
                text += " : ";
                if (reservation.status === "NOT_CONFIRMED") {
                    text+= "Avaiting confirmation (use link in email)";
                } else if (reservation.status === "HAS_SPACE") {
                    text+= reservation.numberOfSeatsReserved + " seat(s) reserved";
                } else if (reservation.status === "WAITING_LIST") {
                    text+= "On waiting list";
                } else  {
                    text+= "Status is unknown";
                }
                return text;
            }


        }]);


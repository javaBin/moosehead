angular.module('mooseheadModule')
    .controller('AdminCtrl', ['$scope', '$http','$location',
        function($scope, $http, $location) {
            $scope.workshops = [];
            $scope.showNoAccess=false;
            $scope.needLogin = false;
            $scope.needAccess = false;
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
                    $http({method: "GET", url: "data/alldata"})
                        .success(function(value) {
                            $scope.workshops = value;
                        });
                });

            $scope.isConfirmed = function(participant) {
                if (participant.isEmailConfirmed) {
                    return "Confirmed";
                }
                return "Not confirmed";
            };

            $scope.queNum = function(workshop,participant) {
                var pos = workshop.participants.indexOf(participant);
                return pos+1;
            };

            $scope.googleLogin = function() {
                var absloc = $location.absUrl();
                var ind=absloc.indexOf("/admin/#");
                var stratpart = absloc.substr(0,ind);
                var newloc = stratpart + "/oauth2callback/login?sendMeTo=" + encodeURIComponent(absloc);
                window.location.href = newloc; // how do I do this with angular?
            };

            $scope.partCancel = function(workshopid,participant) {
                var data = {
                    email: participant.email,
                    workshopid: workshopid,
                    numSpotCanceled: Number(participant.numCancel)
                };
                $http({method: "POST", url: "data/partialCancel",data: data})
                    .success(function(result) {
                        if (result.status === "OK") {
                            participant.message = "Done";
                        } else {
                            participant.message = result.message;
                        }
                    });
            };

            $scope.resendConfirmation = function (workshopid,participant) {
                var payload = {
                    reservationToken: participant.reservationToken,
                    workshopid : workshopid
                };
                $http({method: "POST", url: "data/resendConfirmation",data: payload})
                    .success(function(result) {
                        if (result.status === "OK") {
                            participant.message = "Done";
                        } else {
                            participant.message = result.message;
                        }
                    });

            };

            $scope.alterShowUp = function(participant) {
                var data = {
                    reservationToken : participant.reservationToken,
                    hasShownUp: participant.hasShownUp
                };
                $http({method: "POST", url: "data/shownUp",data: data})
                    .success(function(result) {

                    }).error(function(data, status, headers, config) {
                        alert("Failed to update");
                        console.log(data);
                    });
            };

            $scope.updateNumSpots = function (workshop,numspots) {
                var payload = {
                    workshopid : workshop.id,
                    numspots: numspots
                };
                $http({method: "POST", url: "data/updateWorkshopSize",data: payload})
                    .success(function(result) {
                        if (result.status === 'OK') {
                            window.location.reload();
                        } else {
                            window.alert(result.errormessage);
                        }
                    }).error(function(data, status, headers, config) {
                        alert("Failed to update");
                        console.log(data);
                    });
            };

            $scope.showWaitingListStart = function (workshop,index) {
                var placesLeft = workshop.numberOfSeats;
                for (var ind=0;ind<=index;ind++) {
                    var mySpots = workshop.participants[ind].numberOfSeats;
                    if (placesLeft < mySpots) {
                        if (ind === index) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                    placesLeft = placesLeft - mySpots;
                }

                return false;
            };

            $scope.partClass = function (workshop,index) {
                var placesLeft = workshop.numberOfSeats;
                for (var ind=0;ind<=index;ind++) {
                    var mySpots = workshop.participants[ind].numberOfSeats;
                        if (ind === index) {
                        if (placesLeft < mySpots) {
                            return "redtext";
                        } else {
                            return "";
                        }

                    }
                    placesLeft = placesLeft - mySpots;
                }

                return "";

            };
        }]);


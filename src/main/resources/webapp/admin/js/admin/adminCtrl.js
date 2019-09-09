angular.module('mooseheadModule')
    .controller('AdminCtrl', ['$scope', '$http','$location',
        function($scope, $http, $location) {
            var filterWorkshops = function(allWorkshops,filterText) {
                var includeParticipant = function (partobj) {
                    if (!filterText) {
                        return true;
                    }
                    if (filterText.trim().length === 0) {
                        return true;
                    }
                    if (partobj.name.toLowerCase().indexOf(filterText.toLowerCase()) !== -1) {
                        return true;
                    }
                    if (partobj.email.toLowerCase().indexOf(filterText.toLowerCase()) !== -1) {
                        return true;
                    }
                    return false;
                };
                var filteredWorkshops = [];
                allWorkshops.forEach(function (wsvalue) {
                    var cl = _.clone(wsvalue);
                    cl.participants = [];
                    wsvalue.participants.forEach(function (value) {
                        if (includeParticipant(value)) {
                            cl.participants.push(value);
                        }
                    });
                    filteredWorkshops.push(cl);
                });
                return filteredWorkshops;
            };
            $scope.workshops = [];
            $scope.allWorkshops = [];
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
                            $scope.allWorkshops = value;
                            $scope.workshops = filterWorkshops(value);
                        });
                });

            $scope.doAdminFilter = function() {
                $scope.workshops = filterWorkshops($scope.allWorkshops,$scope.wsfilter);
            };

            $scope.isConfirmed = function(participant) {
                if (participant.isEmailConfirmed) {
                    return "Confirmed";
                }
                return "Not confirmed";
            };

            $scope.queNum = function(workshop,participant) {
                var wsorg = _.findWhere($scope.allWorkshops,{id: workshop.id});
                var pos = wsorg.participants.indexOf(participant);
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

            $scope.confirmToAll = function(workshopid) {
                var data = {
                    workshopid: workshopid
                };
                $http({method: "POST", url: "data/resendReservationConfirmEmail",data: data})
                    .success(function(result) {
                        if (result.status === "OK") {
                            window.alert("Mail sent");
                        } else {
                            window.alert("Error " + result.message);
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


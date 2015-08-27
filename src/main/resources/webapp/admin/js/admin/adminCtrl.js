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
        }]);


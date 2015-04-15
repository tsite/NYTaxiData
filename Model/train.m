#Octave script for predicting the time a taxi ride will take
clear;clc;
1;

%plotLearnCurve = yes_or_no('Plot Learning Curve? ');

display('Loading Training Data...');
fflush(stdout);
data = load('-ascii','trip_data_sample.txt');

X = data(:, 2:end); y = data(:, 1);

%for memory purposes
clear('data');

display('Preparing to train...');
fflush(stdout);

m = length(y); % number of training examples

%Remove distance from feature vector and calculate straight line
%distance based on start and end coordinates

%Straight-line distance
LongPerimDist = X(:,6) - X(:,4);
LatPerimDist = X(:,7) - X(:,5);
Xdist = sqrt((LongPerimDist).^2 + (LatPerimDist).^2);

%Midpoint Coordinate
MPCLong = 1/2*(X(:,4) + X(:,6));
MPCLat = 1/2*(X(:,5) + X(:,7));

X = [X(:,1:2) Xdist LongPerimDist LatPerimDist MPCLong MPCLat X(:,4:end)];


%No need to get the squares of the binary value
%isWeekday. isWeeday = {1,0} and as such the squares
%are also in {1,0}

X = polyTerms(X,9);



n = size(X,2) + 1; %number of features plus the bias feature


%Scale the features [implementation left to featureScale()]

muX = mean(X);
stdevX = std(X);

X_norm = featureScale(X,muX,stdevX);


% Add a column of ones (bias column) to x_norm
X_norm = [ones(m,1),X_norm];

display('Ready.');
fflush(stdout);

% initialize fitting parameters with
%random values in case of symmetry
theta = 10^-4*unifrnd(-1,1,n,1);
%TEST THETA USING PREVIOUS THETA
%theta = load('-ascii','theta_calc.txt');
iterations = 3000;
alpha = .01;
lambda = 0;


% run gradient descent
[theta,cost_history] = conjGradDescent(X_norm, y, theta, lambda, alpha, iterations);
%theta = gradientDescent(X_norm,y,theta,lambda,alpha,iterations);

figure();
plot(100:iterations,cost_history(100:end));
xlabel('Iterations');
ylabel('Sum of squared errors');
title('Cost function vs. no. iterations');

printf('Theta found by gradient descent: \n');

for i=1:n
	printf("\tTheta(%d): %f\n",i-1,theta(i));
end
printf("\n");

errTraining = testTheta(X_norm,y,theta);

printf('Training Error: %.2f Minutes\n',errTraining/60);


%VALIDATE MODEL HERE
%USING VALIDATION DATA SEPARATE FROM THE TRAINING DATA

printf('Loading Validation Data...\n');

validData = load('-ascii','trip_data_valid.txt');
Xval = validData(:,2:end);
yval = validData(:,1);
clear('validData');

mval = size(Xval,1);

Xval_Long = Xval(:,6) - Xval(:,4);
Xval_Lat = Xval(:,7) - Xval(:,5);
Xval_dist = sqrt((Xval_Long).^2 + (Xval_Lat).^2);
MPCLong = 1/2*(Xval(:,4) + Xval(:,6));
MPCLat = 1/2*(Xval(:,5) + Xval(:,7));

Xval = [Xval(:,1:2) Xval_dist Xval_Long Xval_Lat MPCLong MPCLat Xval(:,4:end)];

Xval = polyTerms(Xval,9);



Xval_norm = featureScale(Xval,muX,stdevX);
Xval = [ones(mval,1) Xval_norm];

errValidate = testTheta(Xval,yval,theta);

printf('Cross Validation Error: %.2f Minutes\n',errValidate/60);



%TEST THE MODEL HERE
%USING TESTING DATA SEPARATE FROM THE TRAINING AND
%VALIDATION DATA

display('Loading Test Data...');
fflush(stdout);

testData = load('-ascii','trip_data_test.txt');
Xtest = testData(:,2:end);

mtest = size(Xtest,1);


Xtest_dist = sqrt((Xtest(:,4) - Xtest(:,6)).^2 + (Xtest(:,5) - Xtest(:,7)).^2);

Xtest_Long = Xtest(:,6) - Xtest(:,4);
Xtest_Lat = Xtest(:,7) - Xtest(:,5);
Xtest_dist = sqrt((Xtest_Long).^2 + (Xtest_Lat).^2);


%Midpoint Coordinate
MPCLong = 1/2*(Xtest(:,4) + Xtest(:,6));
MPCLat = 1/2*(Xtest(:,5) + Xtest(:,7));

Xtest = [Xtest(:,1:2) Xtest_dist Xtest_Long Xtest_Lat MPCLong MPCLat Xtest(:,4:end)];


Xtest = polyTerms(Xtest,9);

Xtest_norm = featureScale(Xtest,muX,stdevX);
Xtest_norm = [ones(mtest,1) Xtest_norm];

Ytest = testData(:,1);
clear('testData');

display('Done.');
fflush(stdout);

err = testTheta(Xtest_norm,Ytest,theta);

printf('Average Time Error: %.2f Minutes\n',err/60);

save('-ascii','theta_calc.txt','theta');%
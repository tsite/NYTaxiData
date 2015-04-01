#Octave script for predicting the time a taxi ride will take
clear;clc;
1;

%plotLearnCurve = yes_or_no('Plot Learning Curve? ');

data = load('-ascii','trip_data_sample.txt');

X = data(:, 2:end); y = data(:, 1);

%for memory purposes
clear('data');

m = length(y); % number of training examples

X = [X X.^2 X.^3];

n = size(X,2) + 1; %number of features plus the bias feature


%Scale the features [implementation left to featureScale()]
X_norm = featureScale(X);

 % Add a column of ones (bias column) to x_norm
X_norm = [ones(m,1),X_norm];



% initialize fitting parameters with
%random values in case of symmetry
%theta = 10^-4*unifrnd(-1,1,n,1);
theta = zeros(n,1);
iterations = 800;
alpha = .3;
lambda = 0;


% run gradient descent
theta = gradientDescent(X_norm, y, theta, lambda, alpha, iterations);



printf('Theta found by gradient descent: \n');

for i=1:n
	printf("\tTheta(%d): %f\n",i-1,theta(i));
end
printf("\n");

errTraining = test(X_norm,y,theta);

printf('Training Error: %.2f Minutes\n',errTraining/60);


%VALIDATE MODEL HERE
%USING VALIDATION DATA SEPARATE FROM THE TRAINING DATA


validData = load('-ascii','trip_data_valid.txt');
Xval = validData(:,2:end);
yval = validData(:,1);
clear('validData');

mval = size(Xval,1);

Xval = [Xval Xval.^2 Xval.^3];

Xval_norm = featureScale(Xval);
Xval_norm = [ones(mval,1) Xval_norm];

errValidate = test(Xval_norm,yval,theta);

printf('Cross Validation Error: %.2f Minutes\n',errValidate/60);

%[lvec,errT,errV] = validationCurve(X_norm,y,Xval_norm,yval);
%
%figure();
%hold on;
%plot(lvec,errT);
%plot(lvec,errV);
%hold off;


%if plotLearnCurve == 1
%	[errTrain,errVal] = learnCurve(X_norm,y,Xval_norm,yval,alpha,lambda);
%
%	figure();
%	hold on;
%	plot(1:size(errTrain,1),errTrain);
%	plot(1:size(errVal,1),errVal);
%	hold off;
%end


%TEST THE MODEL HERE
%USING TESTING DATA SEPARATE FROM THE TRAINING AND
%VALIDATION DATA

testData = load('-ascii','trip_data_test.txt');
Xtest = testData(:,2:end);

mtest = size(Xtest,1);

Xtest = [Xtest Xtest.^2 Xtest.^3];

Xtest_norm = featureScale(Xtest);
Xtest_norm = [ones(mtest,1) Xtest_norm];

Ytest = testData(:,1);
clear('testData');

err = test(Xtest_norm,Ytest,theta);

printf('Average Time Error: %.2f minutes\n',err/60);

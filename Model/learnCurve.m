function [errorTrain,errorValidate] = learnCurve(X,y,Xval,Yval,alpha,lambda)

	m = size(y,1);

	for i = 1:10000
		Xtmp = X(1:i,:);
		ytmp = y(1:i,:);

		printf('%d:\n',i);
		fflush(stdout);

		theta = 10^-5*unifrnd(-1,1,size(Xtmp,2),1);
		theta = gradientDescent(Xtmp,ytmp,theta, lambda, alpha, 400);
		
		errorTrain(i) = costFunction(Xtmp,ytmp,theta,0);
		errorValidate(i) = costFunction(Xval,Yval,theta,0);

	end

end
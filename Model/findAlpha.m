alphas = 0

for alpha=.0001:1*10^-4:1
	printf('Alpha: %f\n',alpha);
	theta = zeros(n,1);
	theta = gradientDescent(X_norm,y,theta,lambda,alpha,400);
	err = test(Xtest,Ytest,theta);
	if err < 5*60
		printf('Alpha found: %f\tError: %f',alpha,err/60.0);
		alphas = [alphas alpha];
	end
end

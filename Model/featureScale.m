function X_norm = featureScale(X)

	m = size(X,1);
	n = size(X,2);

	X_norm = zeros(m,n);

	mu = mean(X);
	stdev = std(X);


	for j = 1:n
		Xvec = X(:,j);
		X_norm(:,j) = (Xvec - ones(m,1)*mu(j))/stdev(j);
	end

end
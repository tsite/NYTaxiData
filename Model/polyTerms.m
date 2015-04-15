function X_poly = polyTerms(X,k)

	X_poly = X;

	for i = 2:k
		 X_poly = [X_poly X.^i];
	end

end